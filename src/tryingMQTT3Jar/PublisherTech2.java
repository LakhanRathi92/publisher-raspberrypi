package tryingMQTT3Jar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.lang.InterruptedException;


public class PublisherTech2 {
	public static void main(String[] args) throws IOException{
		System.out.println("Starting Proximity Sensor measurement !");        
        
		
		final GpioController gpio = GpioFactory.getInstance();
        GpioPinListenerDigital listener  = new GpioPinListenerDigital() {
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            // display pin state on console
            System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
    		try {
				sendData(event.getState());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            }
        };        
        GpioPinDigitalInput pin = gpio.provisionDigitalInputPin(RCMPin.GPIO_25, "");
        System.out.println(" --> pin number " + pin);
        System.out.println(" --> GPIO PIN STATE CHANGE: " + pin.getState());
        
         // create and register gpio pin listener
        gpio.addListener(listener, pin);
        
        try{
            while(true) {
                Thread.sleep(500);
            }
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }		
	}
	
	public static int numBytesToEncode(int len) {
        if (0 <= len && len <= 127) return 1;
        if (128 <= len && len <= 16383) return 2;
        if (16384 <= len && len <= 2097151) return 3;
        if (2097152 <= len && len <= 268435455) return 4;
        throw new IllegalArgumentException("value shoul be in the range [0..268435455]");
    }
	
	
	public static void sendData(PinState proximity) throws IOException{
		String topic        = "http://localhost:8000/observations.ttl";
        
		String content1;
		
		if(proximity.isHigh()){
			 content1      = "1"; 
		}
		else
		{
			 content1      = "0"; 
		}
        
        String content2     = "99999999"; // For temperature
        int qos             = 0;
        String broker       = "ws://localhost:8080";
        String clientId     = "JavaSamplePublisher";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+ content1 + " and " + content2);
            
    		
    		
    		/*Actual reading*/
			byte[] read1 = content1.getBytes("UTF-8");	            
			byte[] read2 = content2.getBytes("UTF-8");
			
			
			System.out.println("size of reading 1 " + read1.length);
			System.out.println("size of reading 2 " + read2.length);

          /* creating logical Binary translation Table */
        
    		int uid1 = 123; //ID 123 is for Proximity Sensor
    		
    		String Entry1 = uid1+" "+"0"+" "+ read1.length +" \n"; //123 0 3 (Start at 0 read 3 bytes)
     		byte[] entry1 = Entry1.getBytes();
    		System.out.println(Entry1);
    		
    		int uid2 = 456;
    	
    		String Entry2 = uid2+" "+ read1.length +" "+ read2.length +" \n";//read1.length +" "+ read2.length +" \n"; //(start at 3, )
    		byte[] entry2 = Entry2.getBytes();
    		System.out.println(Entry2);
    		
    		int totalSizeOfBtt = entry1.length + entry2.length;
    		byte[] inByteArray = VariableLengthEnc_Dec.Encode(totalSizeOfBtt);
  		
			
			/* Combine them up */
            ByteArrayOutputStream out = new ByteArrayOutputStream();
                   
            out.write(inByteArray);
            out.write(entry1);
            out.write(entry2);
            out.write(read1);
            out.write(read2);
            
            System.out.println("Econded content size " + out.size() + " Bytes" );
            

            /*Regular MQTT Publish*/	            
            MqttMessage message = new MqttMessage(out.toByteArray());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
	}
	
}
