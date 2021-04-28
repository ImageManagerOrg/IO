package com.io.image.manager.data;

import com.io.image.manager.exceptions.ConversionException;

public class ConversionInfo {
    private final String format;
    private final int rate;
    private float jpgRate;
    private float pngRate;


    public ConversionInfo(String format, int rate) throws ConversionException {
        this.rate = rate;
        if (format.equals("jpg")) {
            if (rate > 100 || rate < 0) throw new ConversionException("Invalid compression rate for JPG format");
            this.format = format;
            this.jpgRate = (float) rate / 100;
        } else if (format.equals("png")) {
            if (rate > 9 || rate < 0) throw new ConversionException("Invalid compression rate for PNG format");
            this.format = format;
            this.pngRate = (float) (9 - rate) / 10;
        } else {
            throw new ConversionException("Unknown image format");
        }
    }

    public String hashString() {
        return format + Integer.toString(rate);
    }

    public String getFormat() {
        return format;
    }

    public float getJpgRate() {
        return jpgRate;
    }

    public float getPngRate() {
        return pngRate;
    }
}
