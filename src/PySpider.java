import java.awt.Robot;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PySpider {
	
	private String HOST = "127.0.0.1";
	private int PORT = 5000;
	private String UTF_8 = "UTF-8";
	private Action ACTION = new Action();
	private String END = "\r\n";
	public Status STATUS = new Status();
	public Format FORMAT = new Format();
	
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
	
	PySpider(){
	}
	PySpider(String host,int port){
		this.HOST = host;
		this.PORT = port;
	}
		
	@SuppressWarnings("resource")
	public String doAction(String action,String path,String data) throws IOException{
		// 建立连接
		InetAddress addr = InetAddress.getByName(this.HOST);
		Socket socket = new Socket(addr, this.PORT);
		// 发送数据头和数据头
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),this.UTF_8));
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
			wr.write(String.format("%s %s HTTP/1.0%s"
					+ "HOST:%s%s"
					+ "%s",action,path,END,
					this.HOST,END,
					END));
		}
		wr.flush();
		// 读取返回信息
		BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream(),this.UTF_8));
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
	
	public String getHost(){
		return this.HOST;
	}
	public int getPort(){
		return this.PORT;
	}
	public String getSaveData(String script)throws IOException{
		return "script="+URLEncoder.encode(script,this.UTF_8);
	}
	public String getRunData(String project)throws IOException{
		String result = new String();
		result += "webdav_mode=false&";
		String task = String.format("{\"process\":{\"callback\":\"on_start\"}, \"project\":\"%s\", \"taskid\":\"data:,on_start\", \"url\":\"data:,on_start\"}",project);
		result += "task="+URLEncoder.encode(task,this.UTF_8)+"&";
		String a = this.GetScript(project);
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String,String> b = mapper.readValue(a, HashMap.class);
		String script = b.get("script");
		result += "script="+URLEncoder.encode(script,this.UTF_8);
		return result;
	}
	/*
	 * 来自debug.py文件
	 */
	//保存python脚本到服务器
	public String SaveScript(String project,String script)throws IOException{
		return this.doAction(this.ACTION.POST,String.format("/debug/%s/save", project),this.getSaveData(script));
	}
	//从服务器下载python脚本
	public String GetScript(String project)throws IOException{
		return this.doAction(this.ACTION.GET,String.format("/debug/%s/get", project),null);
	}
	//获取调试的html主页面
	public String GetDebugHtml(String project)throws IOException{
		return this.doAction(this.ACTION.GET,String.format("/debug/%s", project),null);
	}
	//在调试界面运行项目
	public String DebugRunProject(String project)throws IOException{
		return this.doAction(this.ACTION.POST,String.format("/debug/%s/run", project),this.getRunData(project));
	}
	/*
	 * 来自index.py文件
	 */
	//获取pyspider的html主页
	public String GetIndexHtml() throws IOException{
		return this.doAction(this.ACTION.GET,"/",null);
	}
	//改变项目的运行的状态
	public String ChangeProjectStatus(String project,String status)throws IOException{
		String data = "pk=" + URLEncoder.encode(project,this.UTF_8) + "&name=" + URLEncoder.encode("status",this.UTF_8) + "&value=" + URLEncoder.encode(status,this.UTF_8);
		return this.doAction(this.ACTION.POST,"/update",data);
	}
	//运行爬虫项目
	public String RunProject(String project) throws IOException{
		String data = "project="+URLEncoder.encode(project,this.UTF_8);
		return this.doAction(this.ACTION.POST,"/run",data);
	}
	//获取计数器
	public String GetCounter() throws IOException{
		return this.doAction(this.ACTION.GET,"/counter",null);
	}
	//获取队列
	public String GetQueues() throws IOException{
		return this.doAction(this.ACTION.GET,"/queues",null);
	}
	/*
	 * 来自task.py文件
	 */
	//根据项目的taskid获取任务的详细信息html页面
	public String GetTaskHtml(String project,String taskid) throws IOException{
		return this.doAction(this.ACTION.GET,"/task/"+project+":"+taskid,null);
	}
	//根据项目的taskid获取任务的详细信息的json格式的数据
	public String GetTask(String project,String taskid) throws IOException{
		return this.doAction(this.ACTION.GET,"/task/"+project+":"+taskid+".json",null);
	}
	//获取某个项目的所有任务html界面
	public String GetTasksHtml(String project) throws IOException{
		return this.doAction(this.ACTION.GET,"/tasks?project="+project,null);
	}
	//获取某个项目的所有激活任务html界面
	public String GetActiveTasks(String project) throws IOException{
		return this.doAction(this.ACTION.GET,"/active_tasks?project="+project,null);
	}
	/*
	 * 来自result.py文件
	 */
	//获取某个项目的结果html界面
	public String GetResultsHtml(String project) throws IOException{
		return this.doAction(this.ACTION.GET,"/results?project="+project,null);
	}
	//根据输入的数据格式输出某个项目的结果
	public String GetResultsFormat(String project,String format) throws IOException{
		return this.doAction(this.ACTION.GET,"/results/dump/"+project+"."+format,null);
	}
	//一个典型的运行脚本爬虫的运行方式
	public String StandardRun(String project,String script,String status) throws Exception{
		HashMap<String,Object> result = new HashMap<>();
		result.put("save_script", this.SaveScript(project, script));
		result.put("change_project_status_to_running", this.ChangeProjectStatus(project, status));
		Robot r = new Robot();//执行完上面一条必须等待改变项目的运行状态为运行，才可以正式运行项目
		r.delay(100);//ms
		ObjectMapper mapper = new ObjectMapper();
		result.put("run_project", mapper.readValue(this.RunProject(project),HashMap.class));
		return mapper.writeValueAsString(result);
	}
	
}