/**
 * Copyright (c) 2017 - 2018 wjl, Inc.
 *
 *  You may obtain a copy of the License at:
 *
 *       http://xxxxxx
 *
 */

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PySpider {
	
	private String HOST = "127.0.0.1";
	private int PORT = 5000;
	private String END = "\r\n";
	private Charset CHARSET = new Charset();
	private Action ACTION = new Action();
	private ObjectMapper OBJECTMAPPER = new ObjectMapper();
	private Status STATUS = new Status();
	public Format FORMAT = new Format();
	private ConnectionURL CONNECTIONURL = new ConnectionURL();
	private String LOADMODULES = "from pyspider.libs.base_handler import *\nfrom urllib import request\nimport datetime,time,pymysql,json,requestsrequests,re\n";
	private String CRAWL_CONFIG = "{\"headers\":{\"Proxy-Connection\": \"keep-alive\",\"Pragma\": \"no-cache\",\"Cache-Control\": \"no-cache\",\"User-Agent\": \"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36\",\"Accept\": \"text/css,*/*;q=0.1\",\"DNT\": \"1\",\"Accept-Encoding\": \"gzip, deflate, sdch, br\",\"Accept-Language\": \"zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4\",}}";

	class Charset{
		public String UTF_8 = "UTF-8";
	}
	class Action{
		public String POST = "POST";
		public String GET = "GET";
	}
	
	class Status{
		public String TODO = "TODO";
		public String STOP = "STOP";
		public String CHECKING = "CHECKING";
		public String DEBUG = "DEBUG";
		public String RUNNING = "RUNNING";
	}
	class Format{
		public String JSON = "json";
		public String TXT = "txt";
		public String CSV = "csv";
	}
	class ConnectionURL{
		public String PROJECTDB = String.format("jdbc:sqlite:%s/data/project.db",
				System.getProperty("user.home"));
		public String TASKDB = String.format("jdbc:sqlite:%s/data/task.db",
				System.getProperty("user.home"));
		public String RESULTDB = String.format("jdbc:sqlite:%s/data/result.db",
				System.getProperty("user.home"));
	}
	
	PySpider(){
	}
	PySpider(String host){
		this.HOST = host;
	}
	PySpider(int port){
		this.PORT = port;
	}
	PySpider(String host,int port){
		this.HOST = host;
		this.PORT = port;
	}

	/**
	 * 生成要输出的数据
	 * @param name
	 * @param result
	 * @return
	 * @throws IOException
	 */
	private String generateResult(String name,String type,Object result)throws IOException{
		HashMap<String,Object> json = new HashMap<>();
		json.put("name",name);
		json.put("result_type",type);
		json.put("result",result);
		return this.OBJECTMAPPER.writeValueAsString(json);
	}

	//***********************************
	// Socket HTTP Methods
	//***********************************

	/**
	 * 使用socket进行post或get操作
	 * @param action
	 * @param path
	 * @param data
	 * @return
	 * @throws IOException
	 */
	private String doAction(String action,String path,String data) throws IOException{
		// 建立连接
		InetAddress addr = InetAddress.getByName(this.HOST);
		Socket socket = new Socket(addr, this.PORT);
		// 发送数据头和数据头
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),this.CHARSET.UTF_8));
		if(action.equals(this.ACTION.POST)) {
			String packet = String.format("%s %s HTTP/1.0%s"
					+ "HOST:%s%s"
					+ "Content-Length:%d%s"
					+ "Content-Type:application/x-www-form-urlencoded%s%s"
					+ "%s%s",
					action,path,END,
					this.HOST,END,
					data.length(),END,
					END,END,
					data,END);
			wr.write(packet);
		}else if(action.equals(this.ACTION.GET)) {
			String packet = String.format("%s %s HTTP/1.0%s"
							+ "HOST:%s%s"
							+ "%s",action,path,END,
					this.HOST,END,
					END);
			wr.write(packet);
			System.out.println(packet);
		}
		wr.flush();
		// 读取返回信息
		BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream(),this.CHARSET.UTF_8));
		String result = new String();
		String line = null;
		while ((line = rd.readLine()) != null){
			result+=line+"\n";
		}
		wr.close();
		rd.close();
		//去除socket返回数据包的头部信息
		result = result.substring(result.indexOf("\n\n")+2, result.length());
		return result.substring(0,result.length()-1);
	}

	//***********************************
	// 来自debug.py文件
	//***********************************

	/**
	 * 保存python脚本到服务器
	 * 返回字符串的格式:string
	 * @param project
	 * @param script
	 * @return
	 * @throws IOException
	 */
	private String saveScript(String project,String script)throws IOException{
		return this.doAction(this.ACTION.POST,String.format("/debug/%s/save", project),
						"script="+URLEncoder.encode(script,this.CHARSET.UTF_8));
	}

	/**
	 * 从服务器下载python脚本
	 * 返回字符串的格式:{"name":" ","script":" "}
	 * @param project
	 * @return
	 * @throws IOException
	 */
	private String getScript(String project)throws IOException{
		return this.doAction(this.ACTION.GET,String.format("/debug/%s/get", project),null);
	}

	/**
	 * 获取调试的主页面
	 * @param project
	 * @return
	 * @throws IOException
	 */
	public String getDebugPage(String project)throws IOException{
		return this.doAction(this.ACTION.GET,String.format("/debug/%s", project),null);
	}

	/**
	 * 在调试界面运行项目
	 * @param project
	 * @return
	 * @throws IOException
	 */
	private String debugRunProject(String project)throws IOException{
		String data = new String();
		data += "webdav_mode=false&";
		String task = String.format("{\"process\":{\"callback\":\"on_start\"}, " +
				"\"project\":\"%s\", " +
				"\"taskid\":\"data:,on_start\", " +
				"\"url\":\"data:,on_start\"}",project);
		data += "task="+URLEncoder.encode(task,this.CHARSET.UTF_8)+"&";
		HashMap<String,String> b = this.OBJECTMAPPER.readValue(this.getScript(project), HashMap.class);
		String script = b.get("script");
		data += "script="+URLEncoder.encode(script,this.CHARSET.UTF_8);
		return this.doAction(this.ACTION.POST,String.format("/debug/%s/run", project), data);
	}

	//***********************************
	// 来自index.py文件
	//***********************************

	/**
	 * 获取pyspider的主页
	 * @return
	 * @throws IOException
	 */
	public String getIndexPage() throws IOException{
		return this.doAction(this.ACTION.GET,"/",null);
	}

	/**
	 * 改变项目的运行的状态
	 * 返回字符串的格式:string
	 * @param project
	 * @param status
	 * @return
	 * @throws IOException
	 */
	private String changeProjectStatus(String project,String status)throws IOException{
		String data = "pk=" + URLEncoder.encode(project,this.CHARSET.UTF_8) +
				"&name=" + URLEncoder.encode("status",this.CHARSET.UTF_8) +
				"&value=" + URLEncoder.encode(status,this.CHARSET.UTF_8);
		return this.doAction(this.ACTION.POST,"/update",data);
	}

	/**
	 * 运行爬虫项目
	 * @param project
	 * @return
	 * @throws IOException
	 */
	private String runProject(String project) throws IOException{
		return this.doAction(this.ACTION.POST,"/run","project="+URLEncoder.encode(project,this.CHARSET.UTF_8));
	}

	/**
	 * 获取计数器
	 * 返回字符串的格式:json object
	 * @return
	 * @throws IOException
	 */
	public String getCounter() throws IOException{
		return this.doAction(this.ACTION.GET,"/counter",null);
	}

	/**
	 * 获取队列
	 * 返回字符串的格式:json object
	 * @return
	 * @throws IOException
	 */
	public String getQueues() throws IOException{
		return this.doAction(this.ACTION.GET,"/queues",null);
	}

	//***********************************
	// 来自task.py文件
	//***********************************

	/**
	 * 根据项目的taskid获取任务的详细信息的页面
	 * @param project
	 * @param taskid
	 * @return
	 * @throws IOException
	 */
	public String getTaskPage(String project,String taskid) throws IOException{
		return this.doAction(this.ACTION.GET,"/task/"+project+":"+taskid,null);
	}

	/**
	 * 根据项目的taskid获取任务的详细信息的json格式的数据
	 * 返回字符串的格式:json object
	 * @param project
	 * @param taskid
	 * @return
	 * @throws IOException
	 */
	private String getTask(String project,String taskid) throws IOException{
		return this.doAction(this.ACTION.GET,"/task/"+project+":"+taskid+".json",null);
	}

	/**
	 * 获取某个项目的所有任务的界面
	 * @param project
	 * @return
	 * @throws IOException
	 */
	public String getTasksPage(String project) throws IOException{
		return this.doAction(this.ACTION.GET,"/tasks?project="+project,null);
	}

	/**
	 * 获取某个项目的所有激活任务的数据
	 * 返回字符串的格式:json object
	 * @param project
	 * @return
	 * @throws IOException
	 */
	private String getActiveTasks(String project) throws IOException{
		return this.doAction(this.ACTION.GET,"/active_tasks?project="+project,null);
	}

	//***********************************
	// 来自result.py文件
	//***********************************

	/**
	 * 获取某个项目的结果的界面
	 * @param project
	 * @return
	 * @throws IOException
	 */
	public String getResultsPage(String project) throws IOException{
		return this.doAction(this.ACTION.GET,"/results?project="+project,null);
	}

	/**
	 * 根据输入的数据格式输出某个项目的结果
	 * 根据选择的格式输出数据
	 * 如果是json数据则返回几行json对象用'\n'隔开
	 * @param project
	 * @param format
	 * @return
	 * @throws IOException
	 */
	public String getResultsFormat(String project,String format) throws IOException{
		return this.doAction(this.ACTION.GET,"/results/dump/"+project+"."+format,null);
	}

	//***********************************
	// 自定义方法
	//***********************************

	/**
	 * 获取某个项目的taskid列表
	 * @param project
	 * @return
	 * @throws IOException
	 */
	private ArrayList<String> getTaskIDArray(String project) throws IOException{
		String[] arr = this.getResultsFormat(project,this.FORMAT.JSON).split("\n");
		List<HashMap<String,Object>> list = new ArrayList<>();
		ArrayList<String> results = new ArrayList<>();
		for(String str:arr){
			list.add(this.OBJECTMAPPER.readValue(str,HashMap.class));
		}
		for(HashMap<String,Object> map:list){
			results.add((String)map.get("taskid"));
		}
		return results;
	}

	/**
	 * 随机返回某个项目的taskid
	 * @param project
	 * @return
	 * @throws IOException
	 */
	protected String getRandomTaskID(String project)throws IOException{
		ArrayList<String> list = this.getTaskIDArray(project);
		Random random = new Random();
		return list.get(random.nextInt(list.size()));
	}

	/**
	 * 创建一个项目
	 * @param project
	 * @param script
	 * @return
	 * @throws IOException
	 */
	public String createProject(String project,String script)throws IOException{
		return this.generateResult("create","string",this.saveScript(project,script));
	}

	/**
	 *
	 * @param project
	 * @param script
	 * @return
	 * @throws IOException
	 */
	public String updateProject(String project,String script)throws IOException{
		return this.generateResult("update","string",this.saveScript(project,script));
	}

	/**
	 *
	 * @param project
	 * @param group
	 * @return
	 * @throws IOException
	 */
	public String updateProjectGroup(String project,String group)throws IOException{
		String data = "pk=" + URLEncoder.encode(project,this.CHARSET.UTF_8) +
				"&name=" + URLEncoder.encode("group",this.CHARSET.UTF_8) +
				"&value=" + URLEncoder.encode(group,this.CHARSET.UTF_8);
		return this.doAction(this.ACTION.POST,"/update",data);
	}

	/**
	 *
	 * @param project
	 * @param rate
	 * @param burst
	 * @return
	 * @throws IOException
	 */
	public String updateProjectRate(String project,int rate,int burst)throws IOException{
		String rate_burst = rate+"/"+burst;
		String data = "pk=" + URLEncoder.encode(project,this.CHARSET.UTF_8) +
				"&name=" + URLEncoder.encode("rate",this.CHARSET.UTF_8) +
				"&value=" + URLEncoder.encode(rate_burst,this.CHARSET.UTF_8);
		return this.doAction(this.ACTION.POST,"/update",data);
	}

	/**
	 * 调试一个项目
	 * @param project
	 * @return
	 * @throws IOException
	 */
	public String debugProject(String project)throws IOException{
		return this.generateResult("debug","json_object",
				this.OBJECTMAPPER.readValue(this.debugRunProject(project),Object.class));
	}

	/**
	 * 开启一个项目
	 * @param project
	 * @return
	 * @throws IOException
	 * @throws AWTException
	 */
	public String startProject(String project)throws IOException,AWTException{
		this.changeProjectStatus(project,this.STATUS.RUNNING);
		Robot r = new Robot();//执行完上面一条必须等待改变项目的运行状态为运行，才可以正式运行项目
		r.delay(100);//ms
		HashMap<String,String> map = this.OBJECTMAPPER.readValue(this.runProject(project),HashMap.class);
		return this.generateResult("run", "string",map.get("result"));
	}

	/**
	 * 停止一个项目
	 * @param project
	 * @return
	 * @throws IOException
	 */
	public String stopProject(String project)throws IOException{
		return this.generateResult("stop","string",this.changeProjectStatus(project,this.STATUS.STOP));
	}

	/**
	 * 删除一个项目
	 * @param project
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws AWTException
	 */
	public String removeProject(String project) throws ClassNotFoundException, IOException, AWTException {
		this.stopProject(project);
		Robot r = new Robot();//执行完上面一条必须等待改变项目的运行状态为运行，才可以正式运行项目
		r.delay(1000);//ms
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");
		Connection connection = null;
		try
		{
			// create a database connection
			connection = DriverManager.getConnection(this.CONNECTIONURL.PROJECTDB);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.executeUpdate(String.format("delete from projectdb where name='%s';",project));

			// create a database connection
			connection = DriverManager.getConnection(this.CONNECTIONURL.TASKDB);
			statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.executeUpdate(String.format("drop table taskdb_%s;",project));

			// create a database connection
			connection = DriverManager.getConnection(this.CONNECTIONURL.RESULTDB);
			statement = connection.createStatement();
			statement.setQueryTimeout(30);  // set timeout to 30 sec.
			statement.executeUpdate(String.format("drop table resultdb_%s;",project));

			return this.generateResult("remove","String","ok");

		}catch(SQLException e){
			// if the error message is "out of memory",
			// it probably means no database file is found
			return this.generateResult("remove","String",e.getMessage());
		}finally{
			try{
				if(connection != null)
					connection.close();
			}catch(SQLException e){
				// connection close failed.
				return this.generateResult("remove","String",e.getMessage());
			}
		}
	}


}