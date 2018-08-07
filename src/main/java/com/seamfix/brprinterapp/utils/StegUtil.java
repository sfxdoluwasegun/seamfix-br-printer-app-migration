package com.seamfix.brprinterapp.utils;

import lombok.extern.log4j.Log4j;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * http://naveedmurtuza.blogspot.com.ng/2010/05/steganography-in-java.html
 * <p>
 * Hides a file inside a carrier image
 * using the LSB algorithm.
 *
 * @author Naveed Quadri
 * @version 1.0
 */
@Log4j
public class StegUtil {

    private final String FINGERPRINT_MESSAGE = "MIV1";
    private final String AES_TYPE = "AES";
    private int offset;
    private int width;
    private int height;
    private byte[] carrier;
    private String hiddenMessage;
    private boolean encryption;
    private boolean compression;

    /**
     * Gets the decode message from the carrier.
     * Should be called after #reveal(File carrierDir, File outDir, char[] password)
     *
     * @return the decoded message
     */
    public String getDecodedMessage() {
        return hiddenMessage;
    }

    /**
     * @return true if encryption enabled, false otherwise
     */
    public boolean isEncryption() {
        return encryption;
    }

    /**
     * @param encrypt true to enable encryption, false otherwise
     */
    public void setEncryption(boolean encrypt) {
        this.encryption = encrypt;
    }

    /**
     * @return true if compression enabled, false otherwise
     */
    public boolean isCompression() {
        return compression;
    }

    /**
     * @param compression true to enable compression, false otherwise
     */
    public void setCompression(boolean compression) {
        this.compression = compression;
    }

    /**
     * @param carrierDir directory containing the carrier images
     * @param secretFile absolute path to the secret file
     * @param outputDir  path to save the steg file
     * @param message    message to hide, with the secret file
     * @param password   password to encrypt the secret file and message, ONLY if encryption enabled
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalStateException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public void hide(File carrierDir, File secretFile, File outputDir, String message, char[] password) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        if (!(outputDir.isDirectory() && outputDir.exists())) {
            throw new FileNotFoundException("");
        }
        if (secretFile == null) {
            throw new FileNotFoundException("");
        }
        if (message == null) {
            message = "";
        }
        if (encryption) {
            if (password == null) {
                throw new IllegalArgumentException("Encryption cannot be done with no password");
            }
        }
        byte[] payload = getBytes(secretFile);
        byte[] fingerprinMsg = FINGERPRINT_MESSAGE.getBytes();
        String[] carriers = null;
        String imageFileNameWithoutExt = null;
        String sectretFname = secretFile.getName();
        File imageFile = null;
        int payloadSize = payload.length;
        int freeSpaceInCarrier = 0;
        int _bytesWritten;
        int payloadOffset = 0;
        int fnameLen = sectretFname.length();
        FilterDirectory filter = new FilterDirectory("JPG");
        carriers = carrierDir.list(filter);

        payload = addMessageToPayload(payload, message.getBytes());
        payloadSize += message.getBytes().length;

        //System.out.println("Encryption:" + encryption);
        //System.out.println("Compression:" + compression);
        //System.out.println("Payload Size:" + payloadSize);
        if (compression) {
            payload = compressPayload(payload);
            payloadSize = payload.length;
            //System.out.println("Compressed Size:" + payloadSize);
        }
        if (encryption) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(new String(password).getBytes());

            payload = encryptPayload(payload, md.digest());
            payloadSize = payload.length;
            //System.out.println("Encrypted Size:" + payloadSize);
        }

        //System.out.println("Files Found: " + carriers.length);
        //System.out.println(sectretFname);
        for (int i = 0; i < carriers.length; i++) {
            offset = 0;
            _bytesWritten = 0;
            imageFile = new File(carrierDir + "\\" + carriers[i]);
            imageFileNameWithoutExt = getFilenameWithoutExtension(imageFile.getName());
            //System.out.println(imageFileNameWithoutExt);
            carrier = convertImageToRGBPixels(imageFile);

            freeSpaceInCarrier = carrier.length / 8;
            //System.out.println("FreeSpace In Carrier: " + freeSpaceInCarrier);
            freeSpaceInCarrier -= encode(fingerprinMsg, 4, 0);

            //freeSpaceInCarrier -= encode(getBytes(i), 4, 0);

            if (i == 0) {
                freeSpaceInCarrier -= encode(getBytes(payloadSize), 4, 0);

                freeSpaceInCarrier -= encode(getBytes(fnameLen), 4, 0);

                freeSpaceInCarrier -= encode(sectretFname.getBytes(), sectretFname.getBytes().length, 0);

                freeSpaceInCarrier -= encode(getBytes(message.getBytes().length), 4, 0);
            }


            if (freeSpaceInCarrier < payloadSize) {
                _bytesWritten = encode(payload, freeSpaceInCarrier, payloadOffset);
            } else {
                _bytesWritten = encode(payload, payloadSize, payloadOffset);
            }
            freeSpaceInCarrier -= _bytesWritten;
            //System.out.println("(Payload)Bytes Written: " + _bytesWritten);
            payloadSize -= _bytesWritten;
            payloadOffset += _bytesWritten;
            //System.out.println("Bytes Remaining: " + (payloadSize));
            //System.out.println("Payload Offset: " + payloadOffset);
            ImageIO.write(convertRGBPixelsToImage(carrier), "png", new File(outputDir + "\\" + imageFileNameWithoutExt + ".png"));
            if (payloadSize > 0) {
                //System.out.println("@continue");
                continue;
            } else {
                break;
            }
        }
        if (payloadSize > 0) {
            throw new IllegalArgumentException("Not enough cover images");
        }

    }

    /**
     * encodes the #bytesToWrite bytes payload into the carrier image starting from #payloadOffset
     *
     * @param payload       to hide in the carrier image
     * @param bytesToWrite  number of bytes to write
     * @param payloadOffset a pointer in the payload byte array indicating the position to start encoding from
     * @return number of bytes written
     */
    private int encode(byte[] payload, int bytesToWrite, int payloadOffset) {
        int bytesWritten = 0;
        for (int i = 0; i < bytesToWrite; i++, payloadOffset++) {
            int payloadByte = payload[payloadOffset];
            bytesWritten++;
            for (int bit = 7; bit >= 0; --bit, ++offset) {
                //assign an integer to b,shifted by bit spaces AND 1
                //a single bit of the current byte
                int b = (payloadByte >>> bit) & 1;
                //assign the bit by taking[(previous byte value) AND 0xfe]
                //or bit to
                try {
                    carrier[offset] = (byte) ((carrier[offset] & 0xFE) | b);
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    log.error("ArrayIndexOutOfBoundsException in encode of stegutil", aiobe);
                }
            }
        }
        return bytesWritten;
    }

    /**
     * Appends the message to the end of the payload.
     *
     * @param payload  append the message to this payload
     * @param msgBytes the message to append
     * @return payload + message
     */
    private byte[] addMessageToPayload(byte[] payload, byte[] msgBytes) {
        int totalSize = payload.length + msgBytes.length;
        byte[] _payload = new byte[totalSize];
        for (int i = 0; i < payload.length; i++) {
            _payload[i] = payload[i];
        }
        for (int i = 0; i < totalSize - payload.length; i++) {
            _payload[i + payload.length] = msgBytes[i];
        }
        return _payload;
    }

    /**
     * Extracts the secret file fom the provided steg image(s)
     *
     * @param carrierDir directory containing the steg images
     * @param outDir     directory to place the extracted secret file
     * @param password   password to decrypt the secret file and message, ONLY if the payload was encrypted
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalStateException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public void reveal(File carrierDir, File outDir, char[] password) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        byte payload[] = null;
        byte[] tmp = null;
        int payloadRemaining = 0;
        int fnameSize = 0;
        int payloadSize = 0;
        String fname = null;
        String[] carriers = null;
        int msgLen = 0;
        int bytesToDecodeFromCarrier = 0;
        ArrayList<byte[]> payloadData = new ArrayList<byte[]>();
        FilterDirectory filter = new FilterDirectory("png");
        carriers = carrierDir.list(filter);
        for (int i = 0; i < carriers.length; i++) {
            offset = 0;
            carrier = convertImageToRGBPixels(new File(carrierDir + "\\" + carriers[i]));
            if (!isStegnographed(carrier)) {
                continue;
            }
            //System.out.println("Encryption:" + encryption);
            //System.out.println("Compression:" + compression);
            bytesToDecodeFromCarrier = carrier.length / 8 - 4;// - 4 bcoz we have already decoded the fingerprint
            //System.out.println("Bytes to Decode: " + bytesToDecodeFromCarrier);
            if (i == 0) {
                tmp = decode(carrier, 4); //extracting the payload size
                payloadSize = toInteger(tmp);
                payloadRemaining = payloadSize;
                bytesToDecodeFromCarrier -= 4;
                //System.out.println("Bytes to Decode: " + bytesToDecodeFromCarrier);
                //System.out.println("Payload Size: " + payloadSize);

                tmp = null;
                tmp = decode(carrier, 4); //extracting the size of the filename
                fnameSize = toInteger(tmp);
                bytesToDecodeFromCarrier -= 4;
                //System.out.println("Bytes to Decode: " + bytesToDecodeFromCarrier);

                tmp = null;
                tmp = decode(carrier, fnameSize);
                bytesToDecodeFromCarrier -= fnameSize;
                //System.out.println("Bytes to Decode: " + bytesToDecodeFromCarrier);
                fname = new String(tmp);
                //System.out.println("Filename: " + fname);

                tmp = null;
                tmp = decode(carrier, 4);
                msgLen = toInteger(tmp);
                //System.out.println("Message Length " + msgLen);
                bytesToDecodeFromCarrier -= 4;
                //System.out.println("Bytes to Decode: " + bytesToDecodeFromCarrier);
            }
            if (payloadRemaining > bytesToDecodeFromCarrier) {
                payload = decode(carrier, bytesToDecodeFromCarrier);
                payloadRemaining = payloadRemaining - bytesToDecodeFromCarrier;
            } else {
                payload = decode(carrier, payloadRemaining);
                payloadRemaining = payloadRemaining - payloadRemaining;
            }


            //System.out.println("payload Remaining " + payloadRemaining);
            payloadData.add(payload);
            //payloadData.put(i, payload);
            if (payloadRemaining == 0) {
                break;
            }


        }
        if (payloadRemaining > 0) {
            throw new IllegalArgumentException("Some Stego Files missing!");
        }
        try (FileOutputStream fOutStream = new FileOutputStream(outDir + "\\" + fname)) {
            if (!payloadData.isEmpty()) {
                byte[] secretData = new byte[payloadSize];
                byte[] message;// = new byte[msgLen];
                byte[] secretFile;// = new byte[payloadSize - msgLen];
                int ptr = 0;
                for (int i = 0; i < payloadData.size(); i++) {
                    byte[] tmpArray = payloadData.get(i);
                    for (int j = 0; j < tmpArray.length; j++, ptr++) {
                        secretData[ptr] = tmpArray[j];
                    }
                }
                if (encryption) {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    md.reset();
                    md.update(new String(password).getBytes());
                    secretData = decryptPayload(secretData, md.digest());
                    payloadSize = secretData.length;
                    //System.out.println("Decrypted Size:" + payloadSize);
                }

                if (compression) {
                    secretData = decompressPayload(secretData);
                    payloadSize = secretData.length;
                    //System.out.println("Uncompressed Size:" + payloadSize);
                }
                message = new byte[msgLen];
                secretFile = new byte[payloadSize - msgLen];
                //System.out.println("Data Extracted!!!");
                for (int i = 0; i < payloadSize - msgLen; i++) {
                    secretFile[i] = secretData[i];
                }
                //System.out.println("Got the File");
                for (int j = 0; j < (msgLen); j++) {
                    message[j] = secretData[j + (payloadSize - msgLen)];
                }
                hiddenMessage = new String(message);
                //System.out.println(hiddenMessage);
                fOutStream.write(secretFile);
            }
        }

    }

    /**
     * decodes #bytesToRead bytes from the carrier
     *
     * @param carrier
     * @param bytesToRead
     * @return
     */
    private byte[] decode(byte[] carrier, int bytesToRead) {
        byte[] _decode = new byte[bytesToRead];
        for (int i = 0; i < _decode.length; ++i) {
            for (int bit = 0; bit < 8; ++bit, ++offset) {
                try {
                    _decode[i] = (byte) ((_decode[i] << 1) | (carrier[offset] & 1));
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    log.error("ArrayIndexOutOfBoundsException in decode of stegutil", aiobe);
                }
            }
        }
        return _decode;
    }

    /**
     * Converts a byte array with RGB pixel values to
     * a bufferedImage
     *
     * @param carrier byte array of RGB pixels
     * @return BufferedImage
     */
    private BufferedImage convertRGBPixelsToImage(byte[] carrier) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = {8, 8, 8};
        int[] bOffs = {2, 1, 0}; // band offsets r g b
        int pixelStride = 3; //assuming r, g, b, skip, r, g, b, skip..
        ColorModel colorModel = new ComponentColorModel(
                cs, nBits, false, false,
                Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);
        WritableRaster raster = Raster.createInterleavedRaster(
                new DataBufferByte(carrier, carrier.length), width, height, width * 3, pixelStride, bOffs, null);

        return new BufferedImage(colorModel, raster, false, null);
    }

    /**
     * Converts an Image to RG pixel array
     *
     * @param filename image to convert
     * @return byte array
     * @throws IOException
     */
    private byte[] convertImageToRGBPixels(File filename) throws IOException {
        BufferedImage image = ImageIO.read(filename);
        width = image.getWidth();
        height = image.getHeight();
        BufferedImage clone = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = clone.createGraphics();
        graphics.drawRenderedImage(image, null);
        graphics.dispose();
        image.flush();
        WritableRaster raster = clone.getRaster();
        DataBufferByte buff = (DataBufferByte) raster.getDataBuffer();
        return buff.getData();
    }

    /**
     * Compress the payload
     *
     * @param payload
     * @return compressed payload
     * @throws IOException
     */
    private byte[] compressPayload(byte[] payload) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(bos);
        zos.write(payload);
        zos.finish();
        zos.close();
        bos.close();
        return bos.toByteArray();
    }

    /**
     * decompress the payload
     *
     * @param payload
     * @return decompressed payload
     * @throws IOException
     */
    private byte[] decompressPayload(byte[] payload) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(payload);
        GZIPInputStream zis = new GZIPInputStream(bis);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] dataBuf = new byte[4096];
        int bytes_read = 0;
        while ((bytes_read = zis.read(dataBuf)) > 0) {
            out.write(dataBuf, 0, bytes_read);
        }
        payload = out.toByteArray();
        out.close();
        zis.close();
        bis.close();
        return payload;
    }

    /**
     * Encrypts the paylaod using AES-256
     *
     * @param payload  byte array to encrypt
     * @param password password to hashed to SHA-256
     * @return encrypted payload
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalStateException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private byte[] encryptPayload(byte[] payload, byte[] password) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec key = new SecretKeySpec(password, AES_TYPE);
        Cipher cipher = Cipher.getInstance(AES_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] cipherText = new byte[cipher.getOutputSize(payload.length)];
        int ctLength = cipher.update(payload, 0, payload.length, cipherText, 0);
        ctLength += cipher.doFinal(cipherText, ctLength);
        //System.out.println(new String(cipherText));
        //System.out.println(ctLength);
        return cipherText;
    }

    /**
     * decrypts the payoad
     *
     * @param payload  payload to decrypt
     * @param password hashed using sha-256
     * @return decrypted payload
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalStateException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    private byte[] decryptPayload(byte[] payload, byte[] password) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IllegalStateException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec key = new SecretKeySpec(password, AES_TYPE);
        Cipher cipher = Cipher.getInstance(AES_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainText = new byte[cipher.getOutputSize(payload.length)];
        int ptLength = cipher.update(payload, 0, payload.length, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        //System.out.println(new String(plainText));
        //System.out.println(ptLength);
        //payloadSize = ptLength;
        return plainText;
    }

    /**
     * @param name Filename
     * @return filename without extension
     */
    private String getFilenameWithoutExtension(String name) {
        return name.replaceFirst("[.][^.]+$", "");
    }

    /**
     * Converts a byte array to int
     *
     * @param b byte array to convert
     * @return converted int
     */
    private int toInteger(byte[] b) {
        return b[0] << 24 | (b[1] & 0xFF) << 16 | (b[2] & 0xFF) << 8 | (b[3] & 0xFF);
    }

    /**
     * Converts the contents of the file to byte array
     *
     * @param file Filename
     * @return file converted into byte array
     * @throws IOException
     */
    private byte[] getBytes(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        // Get the size of the file
        long length = file.length();
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            return null;
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * Converts an integer to bytes
     *
     * @param i integer to convert
     * @return
     */
    private byte[] getBytes(int i) {
        return new byte[]{(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i};
    }

    /**
     * Matches the first four bytes of the image to the FINGERPRINT_MESSAGE
     *
     * @param carrier carrier byte array
     * @return true if FINGERPRINT_MESSAGE found, false otherwise
     */
    private boolean isStegnographed(byte[] carrier) {
        byte[] tmp = new byte[4];
        String fingerPrint = null;
        tmp = decode(carrier, 4);
        fingerPrint = new String(tmp);
        if (!fingerPrint.equals(FINGERPRINT_MESSAGE)) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, IOException, ShortBufferException, IllegalBlockSizeException, NoSuchProviderException, InvalidKeyException {
        //useful for loading initial properties

        StegUtil stegUtil = new StegUtil();
        File parent = new File("steg-prep");
        File carrierDir = new File(parent, "image");
        File secretFile = new File(parent, "steg-prep.properties");

        File encodeOutputDir = new File(parent, "encode-result");
        encodeOutputDir.mkdirs();
        File revealOutputDir = new File(parent, "reveal-result");
        revealOutputDir.mkdirs();

        stegUtil.hide(carrierDir, secretFile, encodeOutputDir, null, null);

        stegUtil.reveal(encodeOutputDir, revealOutputDir, null);

        File revealFile = new File(encodeOutputDir, "seamfix-logo-sec.png");
//        FileUtils.copyFile(revealFile, new File("src\\main\\resources\\com\\seamfix\\bioregistra\\img\\seamfix-logo-sec.png"));

    }
}

/**
 * @author Naveed Quadri
 */
class FilterDirectory implements FilenameFilter {


    private String[] formats;

    public FilterDirectory(String ext) {
        this(new String[]{ext});
    }

    public FilterDirectory(String[] formats) {
        this.formats = formats;
    }

    public boolean accept(File dir, String name) {
        // We always allow directories, regardless of their extension
        if (dir.isDirectory()) {
            return true;
        }

        // Ok, it???s a regular file, so check the extension
        name = dir.getName().toLowerCase();
        for (int i = formats.length - 1; i >= 0; i--) {
            if (name.endsWith(formats[i])) {
                return true;
            }
        }
        return false;

    }
}