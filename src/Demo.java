import java.util.ArrayList;

public class Demo {
	
	public static void main(String[] args) throws Exception{
		String script = "#!/usr/bin/env python\n# -*- encoding: utf-8 -*-\n# Created on 2017-08-15 09:15:51\n# Project: baidu\n\nfrom pyspider.libs.base_handler import *\nimport datetime\nimport time\nimport json\nimport pymysql\n\nfilter_word = [\"广告\",\"咨询\",\"百度图片\",\"公司\",\"ppt\"]\nkey_word = [\"环境\",\"污水\",\"河长\",\"水利\",\"政策\"]\n\nclass Handler(BaseHandler):\n    crawl_config = {\n        \"headers\":{\n            \"Proxy-Connection\": \"keep-alive\",\n            \"Pragma\": \"no-cache\",\n            \"Cache-Control\": \"no-cache\",\n            \"User-Agent\": \"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36\",\n            \"Accept\": \"*/*\",\n            \"DNT\": \"1\",\n            \"Accept-Encoding\": \"gzip, deflate, sdch\",\n            \"Accept-Language\": \"zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4\",\n        }\n    }\n    \n\n    @every(minutes=24 * 60 * 10)\n    def on_start(self):\n        conn= pymysql.connect(host='127.0.0.1',port=3306,user='root',passwd='sdn',db='repository',charset='utf8')\n        cur = conn.cursor()\n        cur.execute(\"select * from key_word\")\n        rows = cur.fetchall()\n        conn.commit()\n        cur.close()\n        conn.close()\n        for each in rows:\n            url = \"https://www.baidu.com/s?wd=\"+each[1]\n            self.crawl(url , callback=self.baidu_page , save={\"page\":1})\n\n    @config(age=5 * 24 * 60 * 60)\n    def baidu_page(self, response):\n        #先获取到所有的链接\n        time.sleep(2)\n        for each_div in response.doc(\"DIV#content_left>DIV\").items():\n            text_base_info =  each_div.text()\n            if filter(text_base_info) == 1:\n                self.crawl(each_div('h3>a').attr.href , callback=self.index_page , save={\"title\":each_div('h3>a').text()})\n        \n        for each_a in response.doc(\"DIV#page>a\").items():\n            if each_a.text().find(\"下一页\") != -1:\n                page_num = response.save['page']\n                if int(page_num) < 5:\n                    page_num = page_num + 1\n                    self.crawl(each_a.attr.href , callback=self.baidu_page , save={\"page\":page_num})\n            \n    @config(age=10 * 24 * 60 * 60)\n    def index_page(self, response):\n        self.crawl(response.url , callback=self.detail_page , save={\"title\":response.save[\"title\"]})\n        \n        for each_a in response.doc(\"a\").items():\n            if filter(each_a.text()) == 1 and keyFilter(each_a.text()) == 1:\n                self.crawl(each_a.attr.href , callback=self.index_page , save={\"title\":each_a.text()})\n        \n\n    @config(age=10 * 24 * 60 * 60)\n    def detail_page(self, response):\n        result = {\n            \"url\": response.url,\n            \"title\": response.save['title'],\n            \"text_info\": response.doc(\"body\").text()\n        }\n        html = \"<html><head><meta http-equiv='Content-Type' content='text/html;charset=utf-8'><base href='\"+response.url+\"'></head><body><div style='position:relative'>\"\n        html += response.doc(\"HEAD\").html()\n        html += response.doc(\"BODY\").html()\n        html += \"</div></body></html>\"\n        result[\"html\"] = html\n        return result\n    \n    def on_result(self,result):\n        if not result or not result[\"title\"]:\n            return\n        \n        conn= pymysql.connect(host='127.0.0.1',port=3306,user='root',passwd='sdn',db='repository',charset='utf8')\n        cur = conn.cursor()\n        cur.execute(\"select * from baidu where url = %s\" , result[\"url\"])\n        rows = cur.fetchall()\n        if len(rows) == 0:\n            cur.execute(\"select count(*) from baidu\") ;\n            row = cur.fetchone()\n            num = int(row[0]) + 1\n            file_path = \"/home/quick_picture/\" + str(num) + \".html\"\n            file = open(file_path,\"wt\")\n            file.write(result[\"html\"])\n            file.close()\n            \n            cur.execute(\"insert into baidu(url,title,text_info,quick_picture_file) values(%s,%s,%s,%s)\" , (result[\"url\"] , result[\"title\"],result[\"text_info\"],file_path))\n        conn.commit()\n        cur.close()\n        conn.close()\n        \n        \n        \n    \ndef filter(text):\n    for word in filter_word:\n        x = text.find(word)\n        if x != -1:\n            return 0\n    return 1\n\ndef keyFilter(text):\n    for word in key_word:\n        x = text.find(word)\n        if x != -1:\n            return 1\n    return 0\n";

		String script1 = "#!/usr/bin/env python\n# -*- encoding: utf-8 -*-\n# Created on 2017-10-21 20:34:19\n# Project: wjl\n\nfrom pyspider.libs.base_handler import *\n\n\nclass Handler(BaseHandler):\n    crawl_config = {\n    }\n\n    @every(minutes=24 * 60)\n    def on_start(self):\n        self.crawl('www.baidu.com', callback=self.index_page)\n\n    @config(age=10 * 24 * 60 * 60)\n    def index_page(self, response):\n        for each in response.doc('a[href^=\"http\"]').items():\n            self.crawl(each.attr.href, callback=self.detail_page)\n\n    @config(priority=2)\n    def detail_page(self, response):\n        return {\n            \"url\": response.url,\n            \"title\": response.doc('title').text(),\n        }";
		PySpider pyspider = new PySpider("127.0.0.1");
		String project = "baidu";
		
//		System.out.println(pyspider.getDebugPage("test127"));

//		System.out.println(pyspider.getIndexPage());

//		System.out.println(pyspider.getTaskPage("test127", pyspider.getRandomTaskID("test127")));
//		System.out.println(pyspider.getTasksPage("test127"));

//		System.out.println(pyspider.getResultsPage("test127"));

		System.out.println(pyspider.createProject(project,script));
//		System.out.println(pyspider.updateProject(project,script));
//		System.out.println(pyspider.debugProject(project));
//		System.out.println(pyspider.startProject(project));
//		System.out.println(pyspider.updateProjectGroup(project,"123456"));
//		System.out.println(pyspider.updateProjectRate(project,1,3));
//		System.out.println(pyspider.getCounter());
//		System.out.println(pyspider.getQueues());
//		System.out.println(pyspider.stopProject(project));
//		System.out.println(pyspider.removeProject(project));

	}

}