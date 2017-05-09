package tryingMQTT3Jar;

import java.io.ByteArrayOutputStream;

public class VariableLengthEnc_Dec {
	public static int MAX_LENGTH = 268435455;
	
	public static byte[] Encode(int length){
		/*Debug*/
		System.out.println("Encoding " + length);
		
		if (length > MAX_LENGTH || length < 0){
			return null; 
		}
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();		
		byte[] encodedSizeInBytes = null;
		do
		{
			byte byteRepresentation = (byte) (length % 128);			
			length = length / 128;
			
			if(length > 0)
			{
				byteRepresentation |= 0X80; //Setting last bit as 1
			}			
			byteStream.write(byteRepresentation);
		} while(length > 0);
		
		encodedSizeInBytes = byteStream.toByteArray();
	
		/* Debug */
		System.out.println("The decoded value is " + Decode(encodedSizeInBytes));
		
		return encodedSizeInBytes;			
	}
	
	
	public static int Decode(byte[] decodeIt){
		int multiplier = 1;
		int value = 0;
		byte currentByte;
		int ind = 0;
		do
		{
			currentByte = decodeIt[ind];
			value += (currentByte & 0X7F) * multiplier;
			multiplier *= 128;
			ind++;
		}while((currentByte & 0X80) !=0);
		
		return value;
	}
}