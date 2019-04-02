package manipulator;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

public class ImageManipulator {
	
	public String resizeImage(String encodedFile, int newHeight) throws IOException, IncorrectFileException {
		BufferedImage image = decodeToImage(encodedFile);
		int width = image.getWidth();
        int height = image.getHeight();
        double ratio = (double) width / (double) height;
        
        int newWidth = (int) Math.round(newHeight * ratio);
        
        BufferedImage resized = resize(image, newHeight, newWidth);
        
		return encodeBufferedImageToBase64(resized);
	}
	
	public String packImage(String encodedFile, double newSizeInBytes) throws IOException, IncorrectFileException, IncorrectFileSizeException {
		BufferedImage image = decodeToImage(encodedFile);
		
        int imageBytes = Base64.getDecoder().decode(encodedFile).length;
		
		double multiplier = 1.0;
		if (imageBytes > newSizeInBytes) {
			multiplier = newSizeInBytes / imageBytes;
		}
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		int newWidth = (int) (multiplier * width);
        int newHeight = (int) (multiplier * height);
        
        double ratio = (double) newWidth / (double) newHeight;
        
        if (newWidth < 1 || newHeight < 1) {
        	throw new IncorrectFileSizeException("Requested file size (" + newSizeInBytes + " bytes) is invalid!");
        }	
       
		BufferedImage resized = resize(image, newHeight, newWidth);
		ByteArrayOutputStream os;
        
		while (true) {
			os = writeToByteArrayOS(resized);
			
			if (os.size() > newSizeInBytes) {
				newHeight -= 10;
				newWidth = (int) Math.round(newHeight * ratio);
				
				if (newHeight < 1) {
					newHeight = 1;
				}
				
				if (newWidth < 1) {
					newWidth = 1;
				}
				
				resized = resize(resized, newHeight, newWidth);
				break;
			}
			else {
				newHeight += 10;
				newWidth = (int) Math.round(newHeight * ratio);
				resized = resize(resized, newHeight, newWidth);
			}
		}
		
		return encodeBufferedImageToBase64(resized);
	}
	
	private static BufferedImage resize(BufferedImage image, int newHeight,
			int newWidth) {
		Image tmp = image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
		return resized;
	}

	private String encodeBufferedImageToBase64(BufferedImage image)
			throws IOException {
		ByteArrayOutputStream outputStream = writeToByteArrayOS(image);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
	}

	private static ByteArrayOutputStream writeToByteArrayOS(BufferedImage image)
			throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
		return outputStream;
	}
	
	public static BufferedImage decodeToImage(String imageString) throws IncorrectFileException {
        BufferedImage image = null;
        byte[] imageByte;
            imageByte = Base64.getDecoder().decode(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            try {
				image = ImageIO.read(bis);
				bis.close();
			} catch (IOException e) {
				throw new IncorrectFileException("Invalid image file!", e);
			}
        
        return image;
    }

    public static String encodeFileToBase64Binary(File file) throws IOException {
    	try(FileInputStream fileInputStreamReader = new FileInputStream(file)) {
    		
    		byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            
            return Base64.getEncoder().encodeToString(bytes);
    	}
    }
}