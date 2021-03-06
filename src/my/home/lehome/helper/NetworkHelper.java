package my.home.lehome.helper;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-
 * device
 *
 * Needed manifest permissions :
 *
 * <uses-permission android:name="android.permission.INTERNET" />
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 *
 * Test functions :
 *
 * Utils.getMACAddress("wlan0"); Utils.getMACAddress("eth0");
 * Utils.getIPAddress(true); // IPv4 Utils.getIPAddress(false); // IPv6
 */
public class NetworkHelper {

	/**
	 * Returns MAC address of the given interface name.
	 *
	 * @param interfaceName
	 *            eth0, wlan0 or NULL=use first interface
	 * @return mac address or empty string
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static String getMACAddress(String interfaceName) {
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			throw new IllegalStateException(
					"You use getMACAddress() only after API "
						+ Build.VERSION_CODES.GINGERBREAD);
		}
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				if (interfaceName != null) {
					if (!intf.getName().equalsIgnoreCase(interfaceName))
						continue;
				}
				byte[] mac = intf.getHardwareAddress();
				if (mac == null) return "";
				StringBuilder buf = new StringBuilder();
				for (int idx = 0; idx < mac.length; idx++)
					buf.append(String.format("%02X:", mac[idx]));
				if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
				return buf.toString();
			}
		} catch (SocketException e) {} // for now eat exceptions
		return "";
		/*
		 * try { // this is so Linux hack return
		 * loadFileAsString("/sys/class/net/" +interfaceName +
		 * "/address").toUpperCase().trim(); } catch (IOException ex) { return
		 * null; }
		 */
	}

	/**
	 * Get IP address from first non-localhost interface
	 *
	 * @param ipv4
	 *            true=return ipv4, false=return ipv6
	 * @return address or empty string
	 */
	public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections
					.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf
						.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (useIPv4) {
							if (isIPv4) return sAddr;
						} else {
							if (!isIPv4) {
								// drop ip6 port suffix
								int delim = sAddr.indexOf('%');
								return delim < 0 ? sAddr : sAddr.substring(0,
									delim);
							}
						}
					}
				}
			}
		} catch (SocketException e) {} // for now eat exceptions
		return "";
	}
}