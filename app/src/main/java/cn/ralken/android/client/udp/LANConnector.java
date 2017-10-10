package cn.ralken.android.client.udp;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import cn.ralken.android.client.FullscreenActivity;

public class LANConnector extends Thread {
	private DatagramSocket udpSocket;
	private byte[] data = new byte[256];
	private DatagramPacket udpPacket = new DatagramPacket(data, 256);
	private boolean isTCPConnected = false;
	
	private FullscreenActivity activity;
	private Gson gson = new Gson();
	private boolean enable = true;
	
	public LANConnector(FullscreenActivity activity) {
		this.activity = activity;
	}
	
	@Override
	public void run() {
		try {
			udpSocket = new DatagramSocket(25621);
		} catch (SocketException e) {
			activity.postLog("SocketException: " + e.getMessage());
		}

		while (isEnable()) {
			try {
				activity.postLog("Try to receive UDP broadcast package..");
				udpSocket.receive(udpPacket);
			} catch (Exception e) {
				e.printStackTrace();
				activity.postLog("SocketException: " + e.getMessage());
			}

			if (udpPacket.getLength() != 0 && !isTCPConnected) {
				// Receieved udp broadcast!
				final String ipAddress = udpPacket.getAddress().getHostAddress(); 
				String dataContent = new String(data, 0, udpPacket.getLength());
				
				/*String mHexIpAddress = dataContent.substring(4, 12);
				byte[] ipBytes = hexStringToByteArray(mHexIpAddress);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < ipBytes.length; i++) {
					sb.append(Byte.valueOf(ipBytes[i]).intValue());
					if (i != ipBytes.length - 1) {
						sb.append(".");
					}
				}
				String ip = sb.toString(); */
				activity.postLog("Decode ip address: " + ipAddress );
				
				final int port = Integer.parseInt(dataContent.substring(12, dataContent.length()), 16);
				activity.postLog("Decode port from hex data: " + port);
				activity.postLog("Receieved UDP Package data : " + dataContent);
				
				//DTOBroadCast broadCast = gson.fromJson(dataContent, DTOBroadCast.class);

				// Connect to TCP server
				try {
					isTCPConnected = true;
					
					sendHttpRequest("http://" + ipAddress + ":"+port+"/currentTime");
					
					/*InetAddress serverAddr = InetAddress.getByName(broadCast.ip);
					activity.postLog("Connecting to TCP server...");
					Socket socket = new Socket(serverAddr, broadCast.port);
					socket.setSoTimeout(10 * 1000);
					activity.postLog("TCP Server successfully connected..");
					
					// Write to server request
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					DTOClientRequest request = new DTOClientRequest();
					request.requestCode = 1; // 0-tiem 1-config
					oos.writeObject(gson.toJson(request)); // Send DTOClientRequest to cloud
					activity.postLog("Send command to server with value: " + request.requestCode);
					
					// read the server response message
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					String json = (String) ois.readObject();
					DTOServerResponse response = gson.fromJson(json, DTOServerResponse.class);
					activity.postLog("Received message from server response: " + json);

					oos.close();
					ois.close();
					socket.close();*/
				} catch (Exception e1) {
					e1.printStackTrace();
					activity.postLog("Exception occurs: " + e1.getMessage());
				} finally{
					isTCPConnected = false;
					if(udpSocket != null && !udpSocket.isClosed()){
						udpSocket.close();
						activity.postLog("UDP socket has been closed!");
					}
				}
			}
		}
	}

	private void sendHttpRequest(String url) throws ClientProtocolException, IOException {
		HttpClient httpclient = new DefaultHttpClient();
	    HttpResponse response = httpclient.execute(new HttpGet(url));
	    StatusLine statusLine = response.getStatusLine();
	    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        response.getEntity().writeTo(out);
	        String responseString = out.toString();
	        out.close();
	        //..more logic
	        activity.postLog("Received message from server response: " + responseString +", 功能结束");
	        setEnable(false);
	    } else{
	        //Closes the connection.
	        response.getEntity().getContent().close();
	        throw new IOException(statusLine.getReasonPhrase());
	    }
	}
	
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}

		return data;
	}
	
	public boolean isEnable() {
		return enable;
	}
	
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}
