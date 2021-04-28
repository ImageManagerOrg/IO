package com.io.image.manager.data;

import com.io.image.manager.exceptions.ConversionException;

public class ConversionInfo {
    private final String format;
    private float jpg_rate;
    private float png_rate;


    public ConversionInfo(String format, int rate) throws ConversionException {
        if(format.equals("jpg")){
            if(rate>100 || rate<0) throw new ConversionException("Invalid compression rate for JPG format");
            this.format = format;
            this.jpg_rate = (float)rate/100;
        }else if(format.equals("png")){
            if(rate>9 || rate<0) throw new ConversionException("Invalid compression rate for PNG format");
            this.format = format;
            this.png_rate = (float)(9-rate)/10;
        }else{
            throw new ConversionException("Unknown image format");
        }
    }

    public String getFormat() {
        return format;
    }

    public float getJpg_rate() {
        return jpg_rate;
    }

    public float getPng_rate() {
        return png_rate;
    }
}
