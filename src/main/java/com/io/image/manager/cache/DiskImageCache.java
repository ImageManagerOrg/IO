package com.io.image.manager.cache;

import com.io.image.manager.config.AppConfigurationProperties;
import com.io.image.manager.service.operations.ImageOperation;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
    public Optional<BufferedImage> loadImage(String filename, List<ImageOperation> operations) throws IOException {
        Path path = Path.of(props.getDiskCacheMountPoint(), imageHash(filename, operations));
        if (Files.exists(path)) {
            BufferedImage bufferedImage = ImageIO.read(Files.newInputStream(path));
            return Optional.of(bufferedImage);
        }
        return Optional.empty();
    }

    @Override
    public void storeImage(BufferedImage image, String filename, List<ImageOperation> operations) throws IOException {
        Path path = Path.of(props.getDiskCacheMountPoint(), imageHash(filename, operations));
        Path parentDir = path.getParent();
        if (!Files.exists(parentDir)){
            Files.createDirectories(parentDir);
        }
        ImageIO.write(image, "jpg", Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
    }


    private String imageHash(String filename, List<ImageOperation> operations) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(filename);

        for (var operation: operations) {
            buffer.append(operation.getName());

            operation
                    .getArguments()
                    .forEach((key, value) -> buffer.append(key).append(value));
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(buffer.toString().getBytes());

            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            // This should probably never happen. It's just so intellisense does not complain
            return null;
        }
    }
}
