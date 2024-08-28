import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;

public class DestinationHashGenerator {

	
	private static String findDestination(JsonNode node) {
	    if (node.isObject()) {
	        for (JsonNode child : node) {
	            if (child.has("destination")) {
	                return child.get("destination").asText();
	            } else {
	                String found = findDestination(child);
	                if (found != null) return found;
	            }
	        }
	    } else if (node.isArray()) {
	        for (JsonNode child : node) {
	            String found = findDestination(child);
	            if (found != null) return found;
	        }
	    }
	    return null;
	}

	private static String generateRandomString() {
	    int length = 8;
	    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	    StringBuilder randomString = new StringBuilder(length);
	    for (int i = 0; i < length; i++) {
	        int index = (int) (Math.random() * characters.length());
	        randomString.append(characters.charAt(index));
	    }
	    return randomString.toString();
	}
	
	public String generateHash(String prn, String jsonFilePath) throws IOException, NoSuchAlgorithmException {
        // Read the JSON file content
        FileInputStream fis = new FileInputStream(new File(jsonFilePath));
        String jsonContent = IOUtils.toString(fis, StandardCharsets.UTF_8);
        fis.close();

        // Combine PRN and JSON content
        String combinedData = prn + jsonContent;

        // Generate SHA-256 hash
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(combinedData.getBytes(StandardCharsets.UTF_8));

        // Convert byte array to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

	private static String generateMD5Hash(String input) {
	    try {
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        byte[] messageDigest = md.digest(input.getBytes());
	        StringBuilder hexString = new StringBuilder();
	        for (byte b : messageDigest) {
	            hexString.append(String.format("%02x", b));
	        }
	        return hexString.toString();
	    } catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException(e);
	    }
	}

	
	public static void main(String[] args) {
		if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <240344220077> <src/test/resources/sample.json>");
            System.exit(1);
        }
        String prn = args[0].toLowerCase();
        String filePath = args[1];
        
        
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(new File(filePath));
        } catch (IOException e) {
            System.out.println("Failed to read the JSON file.");
            e.printStackTrace();
            System.exit(1);
        }
        
        String destinationValue = findDestination(rootNode);
        if (destinationValue == null) {
            System.out.println("Destination key not found in the JSON file.");
            System.exit(1);
        }

        String randomString = generateRandomString();
        String toHash = prn + destinationValue + randomString;
        String hash = generateMD5Hash(toHash);
        System.out.println(hash + ";" + randomString);

	}

}
