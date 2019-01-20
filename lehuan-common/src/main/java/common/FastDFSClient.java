package common;

import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;

public class FastDFSClient {

	private TrackerClient trackerClient = null;
	private TrackerServer trackerServer = null;
	private StorageServer storageServer = null;
	private StorageClient1 storageClient = null;
	
	public FastDFSClient(String conf) throws Exception {
		if (conf.contains("classpath:")) {
			conf = conf.replace("classpath:", this.getClass().getResource("/").getPath());
		}
//		初始化配置文件
		ClientGlobal.init(conf);
//		创建TrackerClient 对象
		trackerClient = new TrackerClient();
//		使用 TrackerClient 对象创建连接，获得一个 TrackerServer 对象。
		trackerServer = trackerClient.getConnection();
//		创建一个 StorageServer 的引用，值为 null
		storageServer = null;
//		创建一个 StorageClient 对象，需要两个参数 TrackerServer 对象、StorageServer 的引用,获取存储服务器的客户端对象
		storageClient = new StorageClient1(trackerServer, storageServer);
	}
	
	/**
	 * 上传文件方法
	 * <p>Title: uploadFile</p>
	 * <p>Description: </p>
	 * @param fileName 文件全路径
	 * @param extName 文件扩展名，不包含（.）
	 * @param metas 文件扩展信息
	 * @return
	 * @throws Exception
	 */
	public String uploadFile(String fileName, String extName, NameValuePair[] metas) throws Exception {
		String result = storageClient.upload_file1(fileName, extName, metas);
		return result;
	}
	
	public String uploadFile(String fileName) throws Exception {
		return uploadFile(fileName, null, null);
	}
	
	public String uploadFile(String fileName, String extName) throws Exception {
		return uploadFile(fileName, extName, null);
	}
	
	/**
	 * 上传文件方法
	 * <p>Title: uploadFile</p>
	 * <p>Description: </p>
	 * @param fileContent 文件的内容，字节数组
	 * @param extName 文件扩展名
	 * @param metas 文件扩展信息
	 * @return
	 * @throws Exception
	 */
	public String uploadFile(byte[] fileContent, String extName, NameValuePair[] metas) throws Exception {
		
		String result = storageClient.upload_file1(fileContent, extName, metas);
		return result;
	}
	
	public String uploadFile(byte[] fileContent) throws Exception {
		return uploadFile(fileContent, null, null);
	}
	
	public String uploadFile(byte[] fileContent, String extName) throws Exception {
		return uploadFile(fileContent, extName, null);
	}
}
