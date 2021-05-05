package com.io.image.manager.cache;

import com.io.image.manager.config.AppConfigurationProperties;
import com.io.image.manager.data.ConversionInfo;
import com.io.image.manager.origin.OriginServer;
import com.io.image.manager.service.operations.ImageOperation;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class DiskImageCache implements ImageCache {
    AppConfigurationProperties props;

    public DiskImageCache(AppConfigurationProperties props) {
        this.props = props;
    }

    @Override
    public Optional<BufferedImage> loadImage(OriginServer origin, String filename, List<ImageOperation> operations, ConversionInfo info) throws IOException {
        var cacheResult = checkInCache(origin, filename, operations, info);
        if (cacheResult.isEmpty()) {
            return Optional.empty();
        }

        var path = ((DiskCacheResult)cacheResult.get()).getImageDestination();
        BufferedImage bufferedImage = ImageIO.read(Files.newInputStream(path));
        return Optional.of(bufferedImage);
    }

    @Override
    public Optional<CacheResult> checkInCache(OriginServer origin, String filename, List<ImageOperation> operations, ConversionInfo info) throws IOException {
        var path = imageDirectory(origin, filename, operations, info);

        if (path.isEmpty()) {
            return Optional.empty();
        }

        if (Files.notExists(path.get())) {
            return Optional.empty();
        }

        return Optional.of(new DiskCacheResult(path.get()));
    }

    @Override
    public CacheResult storeImage(OriginServer origin, BufferedImage image, String filename, List<ImageOperation> operations, ConversionInfo info) throws IOException {
        var path = imageDirectory(origin, filename, operations, info);
        if (path.isEmpty()) {
            throw new IOException(String.format("Could not create disk path for file %s with given origin server %s\n", filename, origin));
        }

        Path parentDir = path.get().getParent();
        if (!Files.exists(parentDir)){
            Files.createDirectories(parentDir);
        }

        // TODO: change jpg hardcoded format to take it from filename extension
        ImageIO.write(image, "jpg", Files.newOutputStream(path.get(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));

        return new DiskCacheResult(path.get());
    }

    @Override
    public void purgeOrigin(OriginServer origin) throws IOException {
        var path = originDirectory(origin);
        if (path.isEmpty()) return;

        FileUtils.forceDelete(new File(path.get().toString()));
    }

    @Override
    public void purgeImage(OriginServer origin, String filename) throws IOException {
        var path = originDirectory(origin);
        if (path.isEmpty()) return;

        String withoutExtension = filename;

        if (filename.contains(".")) {
            withoutExtension = filename.substring(0, filename.lastIndexOf("."));
        }

        var imagePath = Path.of(path.get().toString(), withoutExtension);

        var file = new File(imagePath.toString());
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }


    /*
        Returns image hash for given image filename, takes image operations into consideration.

        Note that different order of given operations will result in completely different image hash.
     */
    private String imageHash(String filename, List<ImageOperation> operations, ConversionInfo info) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(filename);

        for (var operation: operations) {
            buffer.append(operation.getName());

            operation
                    .getArguments()
                    .forEach((key, value) -> buffer.append(key).append(value));
        }

        buffer.append(info.hashString());

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(buffer.toString().getBytes());

            return Base64.getEncoder().encodeToString(bytes).replace("/", "");
        } catch (Exception e) {
            // This should probably never happen. It's just so intellisense does not complain
            return null;
        }
    }

    /*
        Returns disk directory under which file should be stored/loaded from.

        Directory is of format:
        {mount point}/{origin server hostname}/{filename without extension}/{image hash}
     */
    private Optional<Path> imageDirectory(OriginServer origin, String filename, List<ImageOperation> operations, ConversionInfo info) {
        var originDir = originDirectory(origin);
        if (originDir.isEmpty()) return Optional.empty();

        var withoutExtension = filename.substring(0, filename.lastIndexOf('.'));
        var hash = imageHash(filename, operations, info);

        return Optional.of(Path.of(originDir.get().toString(), withoutExtension, hash));
    }

    private Optional<Path> originDirectory(OriginServer origin) {
        try {
            var originUri = new URI(origin.getUrl());
            var host = originUri.getHost();

            return Optional.of(Path.of(props.getDiskCacheMountPoint(), host));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }
}
