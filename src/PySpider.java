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
	private int SERVER_PORT = 5000;
	private int DB_PORT = 3306;
	private String user = "root";
	private String passwd = "sdn";
	private String db = "repository";
	private String END = "\r\n";
	private Charset CHARSET = new Charset();
	private Action ACTION = new Action();
	private ObjectMapper OBJECTMAPPER = new ObjectMapper();
	private Status STATUS = new Status();
	public Format FORMAT = new Format();
	private ConnectionURL CONNECTIONURL = new ConnectionURL();
	public WanFang WANFANG = new WanFang();
	public String Current_Type = new String();

	class WanFang{
		public String name = "wanfang";
		public ArrayList<String> key_words = new ArrayList<String>(){{
			add("河长制");add("智慧治水");add("水利厅");add("污水治理");add("一河一策");add("水政执法");
			add("水资源保护");add("河湖水域岸线保护");add("水污染防治");add("水生态修复");add("水利工程管理");
			add("环境改善");add("水质监测");add("涉河水利工程");add("岸线规划");add("污水治理");add("水环境治理");
		}};
		public String spider_script = "from pyspider.libs.base_handler import *\n" +
				"import time\n" +
				"import datetime\n" +
				"import pymysql\n" +
				"import re\n" +
				"import sys\n" +
				"reload(sys)\n" +
				"sys.setdefaultencoding('utf8')\n" +
				"\n" +
				"\n" +
				"class Handler(BaseHandler):\n" +
				"    \n" +
				"    crawl_config = {\n" +
				"    }\n" +
				"\n" +
				"    @every(minutes=24 * 60 * 10)\n" +
				"    def on_start(self):\n" +
				"        for key in key_words:\n" +
				"            self.crawl('s.wanfangdata.com.cn/Paper.aspx?q='+key+'&f=top', callback=self.index_page)\n" +
				"            for i in range(2,50):\n" +
				"                self.crawl('s.wanfangdata.com.cn/Paper.aspx?q='+key+'&f=top&p='+str(i), callback=self.index_page)\n" +
				"\n" +
				"    @config(age=24 * 60 * 60)\n" +
				"    def index_page(self, response):\n" +
				"        for each in response.doc(\"a[href^='http://d.wanfangdata.com.cn/Periodical/']\").items():\n" +
				"            self.crawl(each.attr.href , callback=self.detail_page)\n" +
				"                \n" +
				"    @config(priority=6)\n" +
				"    def detail_page(self, response):\n" +
				"        result = {}\n" +
				"        result[\"url\"] = response.url\n" +
				"        result[\"title\"] = response.doc('HTML>BODY>DIV.fixed-width.baseinfo.clear>DIV.section-baseinfo>h1').text()\n" +
				"        result[\"abstract\"] = response.doc('HTML>BODY>DIV.fixed-width.baseinfo.clear>DIV.section-baseinfo>DIV.baseinfo-feild.abstract>DIV.row.clear.zh>DIV.text').text()\n" +
				"        result[\"author\"] = response.doc('HTML>BODY>DIV.fixed-width-wrap.fixed-width-wrap-feild>DIV.fixed-width.baseinfo-feild>DIV.row.row-author>SPAN.text').text()\n" +
				"        result[\"magezine\"] = response.doc('HTML>BODY>DIV.fixed-width-wrap.fixed-width-wrap-feild>DIV.fixed-width.baseinfo-feild>DIV.row.row-magazineName>SPAN.text').text()\n" +
				"        result[\"file_url\"] = ''.join(x.attr.href for x in response.doc('HTML>BODY>DIV.fixed-width.baseinfo.clear>DIV.section-baseinfo>DIV.record-action-link.clear>A.download').items())\n" +
				"        result[\"keyword\"] = response.doc('HTML>BODY>DIV.fixed-width-wrap.fixed-width-wrap-feild>DIV.fixed-width.baseinfo-feild>DIV.row.row-keyword>SPAN.text').text()\n" +
				"        result[\"spider_time\"] = datetime.datetime.now()\n" +
				"        result[\"unit\"] = \"\"\n" +
				"        result[\"time\"] = \"\"\n" +
				"        for i in range(0,12):\n" +
				"            title = response.doc('HTML>BODY>DIV.fixed-width-wrap.fixed-width-wrap-feild>DIV.fixed-width.baseinfo-feild>DIV:nth-child('+str(i)+')>SPAN.pre').text()\n" +
				"            if title == \"作者单位：\":\n" +
				"                result[\"unit\"] = response.doc('HTML>BODY>DIV.fixed-width-wrap.fixed-width-wrap-feild>DIV.fixed-width.baseinfo-feild>DIV:nth-child('+str(i)+')>SPAN.text').text()\n" +
				"            if title == \"在线出版日期：\":\n" +
				"                result[\"time\"] = response.doc('HTML>BODY>DIV.fixed-width-wrap.fixed-width-wrap-feild>DIV.fixed-width.baseinfo-feild>DIV:nth-child('+str(i)+')>SPAN.text').text()\n" +
				"            \n" +
				"        return result\n" +
				"    \n" +
				"    def on_result(self,result):\n" +
				"        if not result or not result['title']:\n" +
				"            return\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        \n" +
				"        #先查找是否存在\n" +
				"        cur.execute(\"select * from wanfang where url = %s\" , result[\"url\"])\n" +
				"        rows = cur.fetchall()\n" +
				"        if len(rows) == 0:\n" +
				"            cur.execute(\"insert into wanfang(title,abstract,author,unit,magezine,file_url,time,keyword,url,spider_time) values(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)\" , (result[\"title\"],result[\"abstract\"],result[\"author\"],result[\"unit\"],result[\"magezine\"],result[\"file_url\"],result[\"time\"],result[\"keyword\"],result[\"url\"],result[\"spider_time\"]))\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()";
		public String paper_script = "from pyspider.libs.base_handler import *\n" +
				"import requests\n" +
				"import pymysql\n" +
				"import sys\n" +
				"import os\n" +
				"reload(sys)\n" +
				"sys.setdefaultencoding('utf8')\n" +
				"\n" +
				"class Handler(BaseHandler):\n" +
				"    crawl_config = {\n" +
				"    }\n" +
				"\n" +
				"    id = \"\"\n" +
				"    \n" +
				"    @every(minutes=24 * 60)\n" +
				"    def on_start(self):\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        \n" +
				"        cur.execute(\"select * from wanfang where paper_flag = 0 limit 300\") ;\n" +
				"        rows = cur.fetchall()\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        \n" +
				"        for row in rows:\n" +
				"            self.crawl(row[6], callback=self.index_page , save={'id':row[0],'fileName':row[9][row[9].rindex('/')+1:len(row[9])]})\n" +
				"\n" +
				"    @config(age=10 * 24 * 60 * 60)\n" +
				"    def index_page(self, response):\n" +
				"        for each in response.doc('a#doDownload').items():\n" +
				"            fileUrl = each.attr.href\n" +
				"            fileName = os.getcwd()+\"/wanfang/\"+response.save['fileName']+\".pdf\"\n" +
				"            rowId = response.save['id']\n" +
				"            #with request.urlopen(fileUrl) as web:\n" +
				"            #    with open(fileName , 'wb') as outfile:\n" +
				"            #        outfile.write(web.read())\n" +
				"            res = requests.get(fileUrl)\n" +
				"            res.raise_for_status()\n" +
				"            playFile = open(fileName, 'wb')\n" +
				"            for chunk in res.iter_content(100000):\n" +
				"                playFile.write(chunk)\n" +
				"            playFile.close()\n" +
				"            conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"            cur = conn.cursor()\n" +
				"            cur.execute(\"update wanfang set file_path = '%s' , paper_flag = 1 where id = %s\" % (fileName , int(rowId)))\n" +
				"            conn.commit()\n" +
				"            cur.close()\n" +
				"            conn.close()\n" +
				"\n" +
				"    @config(priority=2)\n" +
				"    def detail_page(self, response):\n" +
				"        return {\n" +
				"            \"result\" : \"ok\"\n" +
				"        }\n" +
				"\n" +
				"\n";
	}
	public _19Lou _19LOU = new _19Lou();
	class _19Lou{
		String name = "19lou";
		String script = "host = \"127.0.0.1\"\n" +
				"port = 3306\n" +
				"user = \"root\"\n" +
				"passwd = \"sdn\"\n" +
				"db = \"repository\"\n" +
				"\n" +
				"\n" +
				"from pyspider.libs.base_handler import *\n" +
				"import datetime\n" +
				"import time\n" +
				"import pymysql\n" +
				"import json\n" +
				"import sys\n" +
				"reload(sys)\n" +
				"sys.setdefaultencoding('utf8')\n" +
				"\n" +
				"class Handler(BaseHandler):\n" +
				"    crawl_config = {\n" +
				"        \"headers\" : {   \n" +
				"        'Accept':'text/css,*/*;q=0.1',  \n" +
				"        'Accept-Encoding':'gzip, deflate, sdch, br',  \n" +
				"        'Accept-Language':'zh-CN,zh;q=0.8',  \n" +
				"        'Cache-Control':'no-cache',  \n" +
				"        'Connection':'keep-alive',  \n" +
				"        'User-Agent' : 'Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.87 Safari/537.36' ,  \n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    \n" +
				"    \n" +
				"    @every(minutes=60)\n" +
				"    def on_start(self):\n" +
				"        self.spiderNoteUrl()\n" +
				"        self.spiderContext()\n" +
				"        self.spiderNoteNum()\n" +
				"        self.spiderComment()\n" +
				"        \n" +
				"     \n" +
				"    @config(priority=10,age=0)\n" +
				"    def spiderNoteUrl(self):\n" +
				"        prefix = \"http://www.19lou.com/forum-269-\"\n" +
				"        postfix = \".html\"\n" +
				"        for i in range(1,41):\n" +
				"            url = prefix + str(i) + postfix\n" +
				"            self.crawl(url , fetch_type='js' , callback=self.listNotePage)\n" +
				"    \n" +
				"    @config(priority=9,age=0)\n" +
				"    def listNotePage(self , response):\n" +
				"        for each_tbody in response.doc('tbody').items():\n" +
				"            note_url = each_tbody('tr>th>div>a').attr.href\n" +
				"            note_title = each_tbody('tr>th>div>a').attr.title\n" +
				"            author_href = each_tbody('td.author>a').attr.href\n" +
				"            author_name = each_tbody('td.author>a').attr.title\n" +
				"            author_push_time = each_tbody('td.author>span.color9').text()\n" +
				"            update_time = each_tbody('td.lastpost>span.numeral').text()\n" +
				"            self.insertNote(note_url , note_title , author_href , author_name , author_push_time , update_time)\n" +
				"            \n" +
				"    def insertNote(self , note_url , note_title , author_href , author_name , push_time , update_time):\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"\n" +
				"        #检查url和title是否有重复\n" +
				"        cur.execute(\"select * from note where note_title = %s or note_url = %s\", (note_title , note_url))\n" +
				"        rows = cur.fetchall()\n" +
				"        if len(rows) == 0:\n" +
				"            #插入一个新的\n" +
				"            author_id = \"\" ;\n" +
				"            #判断一个发帖人是否有重复\n" +
				"            cur.execute(\"select * from author where author_url = %s \" , (author_href))\n" +
				"            author_rows = cur.fetchall()\n" +
				"            if len(author_rows) == 0:\n" +
				"                #插入一个新作者\n" +
				"                cur.execute(\"insert into author(author_url,author_name) values(%s,%s)\" , (author_href , author_name))\n" +
				"                cur.execute(\"select * from author where author_url = %s \", (author_href))\n" +
				"                author_rows = cur.fetchall()\n" +
				"                for author_row in author_rows:\n" +
				"                    author_id = author_row[0]\n" +
				"            else :\n" +
				"                #获取作者的id\n" +
				"                for author_row in author_rows:\n" +
				"                    author_id = author_row[0]\n" +
				"            now_time = datetime.datetime.now().strftime('%Y-%m-%d')\n" +
				"            cur.execute(\"insert into note(note_title,note_url,note_push_time,note_update_time,note_spider_time,note_push_person_id) values(%s,%s,%s,%s,%s,%s)\",(note_title,note_url,push_time,push_time,now_time,author_id))\n" +
				"            conn.commit()\n" +
				"            cur.close()\n" +
				"            conn.close()\n" +
				"    \n" +
				"    @config(priority=7)\n" +
				"    def spiderContext(self):\n" +
				"        noteList = self.getContextIsNullNote()\n" +
				"        for row in noteList:\n" +
				"            id = row[0]\n" +
				"            url = row[1]\n" +
				"            self.crawl(url , fetch_type='js' , callback=self.getNoteContext , save={\"id\":id})\n" +
				"    \n" +
				"    def getContextIsNullNote(self):\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        cur.execute(\"select note_id , note_url from note where note_context is null\")\n" +
				"        rows = cur.fetchall()\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        return rows\n" +
				"    \n" +
				"    @config(priority=6,age=0)\n" +
				"    def getNoteContext(self,response):\n" +
				"        json_str=\"\"\n" +
				"        for floor in response.doc('DIV.clearall.floor.first').items():\n" +
				"            for content in floor('DIV.thread-cont').items():\n" +
				"                wordStr = content.text()\n" +
				"                imgList = []\n" +
				"                for img in content('img').items():\n" +
				"                    imgList.append(img.attr.src)\n" +
				"                json_obj = {\"word\":wordStr,\"img\":imgList}\n" +
				"                json_str = json.dumps(json_obj)\n" +
				"                break\n" +
				"            break\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        cur.execute(\"update note set note_context = %s where note_id = %s\" , (json_str , response.save[\"id\"]))\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        \n" +
				"    @config(priority=5,age=0)\n" +
				"    def spiderNoteNum(self):\n" +
				"        auditedNoteList = self.getAuditNote()\n" +
				"        for note in auditedNoteList:\n" +
				"            id = note[0]\n" +
				"            url = note[1]+\"?timestamp=\"+str(int(time.time()))\n" +
				"            self.crawl(url , fetch_type='js' , callback=self.getNoteNums , save={\"id\":id})\n" +
				"    \n" +
				"    @config(priority=4,age=0)\n" +
				"    def getNoteNums(self,response):\n" +
				"        for ul in response.doc('ul.fr.clearall.color9.view-hd-num').items():\n" +
				"            lookNum = ul('li:nth-child(1)>i').text()\n" +
				"            replyNum = ul('li:nth-child(2)>i').text()\n" +
				"            print(lookNum+\" \"+replyNum)\n" +
				"            self.insertTrend(response.save[\"id\"],lookNum,replyNum)\n" +
				"            break\n" +
				"    \n" +
				"    def insertTrend(self,id,lookNum,replyNum):\n" +
				"        spiderTime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M')\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"\n" +
				"        cur.execute(\"insert into note_trend(note_id , count_time , look_num , comment_num) VALUES (%s,%s,%s,%s)\" ,\n" +
				"                (str(id) , spiderTime , str(lookNum) , str(replyNum)))\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"    \n" +
				"    def getAuditNote(self):\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        cur.execute(\"select note_id , note_url from note where note_audit = 1\")\n" +
				"        rows = cur.fetchall()\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        return rows\n" +
				"    \n" +
				"    @config(priority=3,age=0)\n" +
				"    def spiderComment(self):\n" +
				"        auditedNoteList = self.getAuditNote()\n" +
				"        for note in auditedNoteList:\n" +
				"            id = note[0]\n" +
				"            url = note[1]+\"?timestamp=\"+str(int(time.time()))\n" +
				"            if self.judgeCommentNum(id):\n" +
				"                self.crawl(url , fetch_type='js' , callback=self.analysisPageOne , save={\"id\":id,\"url\":url})\n" +
				"             \n" +
				"    @config(priority=2,age=0)\n" +
				"    def analysisPageOne(self,response):\n" +
				"        page_info = response.doc('a.page-last').items()\n" +
				"        pageNum = 1\n" +
				"        for page in page_info:\n" +
				"            pageNum = int(page.attr.href.split('-')[4])\n" +
				"            break\n" +
				"        result = self.getTimeAndLou(response.save[\"id\"])\n" +
				"        maxCount = result[\"maxCount\"]\n" +
				"        time = result[\"time\"]\n" +
				"        url = response.save[\"url\"]\n" +
				"        for i in range(1 , pageNum+1):\n" +
				"            pageUrl = url[0:len(url)-8] + str(i) + \"-1.html\"\n" +
				"            self.crawl(pageUrl , fetch_type='js' , callback=self.catchComment , save={\"pageNum\":i,\"count\":maxCount,\"timeYu\":time,\"note_id\":response.save[\"id\"]})\n" +
				"    \n" +
				"    def insertComment(id , author_href , author_name , time , json_str):\n" +
				"        lou_id = self.getLouId(id)\n" +
				"        author_id = self.insertAuthor(author_href , author_name)\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        author_id = insertAuthor(author)\n" +
				"        spider_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M')\n" +
				"        cur.execute(\"insert into note_comment values(%s,%s,%s,%s,%s,%s)\" ,\n" +
				"                (str(id),str(lou_id),str(json_str),str(time),str(spider_time),str(author_id)))\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"    \n" +
				"    def getLouId(self , id):\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        cur.execute(\"select max(comment_id) from note_comment where note_id = %s\",(id))\n" +
				"        rows = cur.fetchall()\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        return int(rows[0][0])+1\n" +
				"    \n" +
				"    def insertAuthor(self,author_href,author_name):\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        author_id = 0\n" +
				"        cur.execute(\"select * from author where author_name = %s\", (author_name))\n" +
				"        rows = cur.fetchall()\n" +
				"        if len(rows) == 0:\n" +
				"            cur.execute(\"insert into author(author_url,author_name) values(%s,%s)\",\n" +
				"                    (author_href , author_name))\n" +
				"            cur.execute(\"select * from author where author_name = %s\", (author_name))\n" +
				"            rows = cur.fetchall()\n" +
				"        author_id = int(rows[0][0])\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        return author_id\n" +
				"    \n" +
				"    @config(priority=1,age=0)\n" +
				"    def catchComment(self,response):\n" +
				"        pageNum = int(response.save[\"pageNum\"])\n" +
				"        if pageNum != 1:\n" +
				"            for floor_info in response.doc(\"DIV.clearall.floor.first\").items():\n" +
				"                time = floor_info('DIV.cont-hd.clearall>p.fl.link1').text()\n" +
				"                author_href = floor_info('DIV.uname>a').attr.href\n" +
				"                author_href = author_href[2:len(author_href)]\n" +
				"                author_name = floor_info('DIV.uname>a').attr.title\n" +
				"                for content_info in floor_info('DIV.thread-cont').items():\n" +
				"                    for s in content_info('dl'):\n" +
				"                        s.extract()\n" +
				"                    wordStr = content_info.text()\n" +
				"                    wordStr = wordStr.replace('\\n', '')\n" +
				"                    wordStr = wordStr.replace('\\t', '')\n" +
				"                    wordStr = wordStr.replace(' ', '')\n" +
				"                    imgList = []\n" +
				"                    for img_info in content_info('img').items():\n" +
				"                        imgList.append(img_info.attr.src)\n" +
				"                    json_obj = {'word': wordStr, 'img': imgList}\n" +
				"                    json_str = json.dumps(json_obj)\n" +
				"                    if time > response.save[\"timeYu\"]:\n" +
				"                        self.insertComment(response.save[\"note_id\"] , author_href , author_name , time , json_str)\n" +
				"                    break\n" +
				"                break\n" +
				"        for floor_info in response.doc(\"DIV.clearall.floor\").items():\n" +
				"            time = floor_info('DIV.cont-hd.clearall>p.fl.link1').text()\n" +
				"            author_href = floor_info('DIV.uname>a').attr.href\n" +
				"            author_href = author_href[2:len(author_href)]\n" +
				"            author_name = floor_info('DIV.uname>a').attr.title\n" +
				"            for content_info in floor_info('DIV.thread-cont').items():\n" +
				"                for s in content_info('dl'):\n" +
				"                    s.extract()\n" +
				"                wordStr = content_info.text()\n" +
				"                wordStr = wordStr.replace('\\n', '')\n" +
				"                wordStr = wordStr.replace('\\t', '')\n" +
				"                wordStr = wordStr.replace(' ', '')\n" +
				"                imgList = []\n" +
				"                for img_info in content_info('img').items():\n" +
				"                    imgList.append(img_info.attr.src)\n" +
				"                json_obj = {'word': wordStr, 'img': imgList}\n" +
				"                json_str = json.dumps(json_obj)\n" +
				"                if time > response.save[\"timeYu\"]:\n" +
				"                    self.insertComment(response.save[\"note_id\"] , author_href , author_name , time , json_str)\n" +
				"                break\n" +
				"            break\n" +
				"                \n" +
				"    def judgeCommentNum(self , id):\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        cur.execute(\"select * from note_trend where note_id = %s order by count_time desc limit 2\" , (str(id),))\n" +
				"        rows = cur.fetchall()\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        if len(rows) == 0 or len(rows) == 1:\n" +
				"            return True\n" +
				"        if rows[0][3] == rows[1][3] :\n" +
				"            return True\n" +
				"        return False\n" +
				"\n" +
				"    def getTimeAndLou(self , id):\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        cur.execute(\"select comment_id , comment_push_time from note_comment where note_id = %s order by comment_id desc limit 1\" , (str(id) ,))\n" +
				"        rows = cur.fetchall()\n" +
				"        result = {}\n" +
				"        if len(rows) == 0:\n" +
				"            result[\"maxCount\"] = 0\n" +
				"            cur.execute(\"select note_push_time from note where note_id = %s\" , (str(id) ,) )\n" +
				"            rows = cur.fetchall()\n" +
				"            result[\"time\"] = str(rows[0][0]) + \" 00:00\"\n" +
				"        else :\n" +
				"            result[\"maxCount\"] = int(rows[0][0]) + 1\n" +
				"            result[\"time\"] = rows[0][1]\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        return result\n" +
				"    \n" +
				"    @config(age=1)\n" +
				"    def index_page(self, response):\n" +
				"        for each in response.doc('a[href^=\"http\"]').items():\n" +
				"            self.crawl(each.attr.href, callback=self.detail_page)\n" +
				"\n" +
				"    @config(priority=2)\n" +
				"    def detail_page(self, response):\n" +
				"        return {\n" +
				"            \"url\": response.url,\n" +
				"            \"title\": response.doc('title').text(),\n" +
				"        }\n";
	}

	public Baidu BAIDU = new Baidu();
	class Baidu{
		String name = "baidu";
		String script = "from pyspider.libs.base_handler import *\n" +
				"import datetime\n" +
				"import time\n" +
				"import json\n" +
				"import pymysql\n" +
				"import sys\n" +
				"reload(sys)\n" +
				"sys.setdefaultencoding('utf8')\n" +
				"\n" +
				"class Handler(BaseHandler):\n" +
				"    crawl_config = {\n" +
				"        \"headers\":{\n" +
				"            \"Proxy-Connection\": \"keep-alive\",\n" +
				"            \"Pragma\": \"no-cache\",\n" +
				"            \"Cache-Control\": \"no-cache\",\n" +
				"            \"User-Agent\": \"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36\",\n" +
				"            \"Accept\": \"*/*\",\n" +
				"            \"DNT\": \"1\",\n" +
				"            \"Accept-Encoding\": \"gzip, deflate, sdch\",\n" +
				"            \"Accept-Language\": \"zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4\",\n" +
				"        }\n" +
				"    }\n" +
				"    \n" +
				"\n" +
				"    @every(minutes=24 * 60 * 10)\n" +
				"    def on_start(self):\n" +
				"        conn= pymysql.connect(host='127.0.0.1',port=3306,user='root',passwd='sdn',db='repository',charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        cur.execute(\"select * from key_word\")\n" +
				"        rows = cur.fetchall()\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        for each in rows:\n" +
				"            url = \"https://www.baidu.com/s?wd=\"+each[1]\n" +
				"            self.crawl(url , callback=self.baidu_page , save={\"page\":1})\n" +
				"\n" +
				"    @config(age=5 * 24 * 60 * 60)\n" +
				"    def baidu_page(self, response):\n" +
				"        #先获取到所有的链接\n" +
				"        time.sleep(2)\n" +
				"        for each_div in response.doc(\"DIV#content_left>DIV\").items():\n" +
				"            text_base_info =  each_div.text()\n" +
				"            if filter(text_base_info) == 1:\n" +
				"                self.crawl(each_div('h3>a').attr.href , callback=self.index_page , save={\"title\":each_div('h3>a').text()})\n" +
				"        \n" +
				"        for each_a in response.doc(\"DIV#page>a\").items():\n" +
				"            if each_a.text().find(\"下一页\") != -1:\n" +
				"                page_num = response.save['page']\n" +
				"                if int(page_num) < 5:\n" +
				"                    page_num = page_num + 1\n" +
				"                    self.crawl(each_a.attr.href , callback=self.baidu_page , save={\"page\":page_num})\n" +
				"            \n" +
				"    @config(age=10 * 24 * 60 * 60)\n" +
				"    def index_page(self, response):\n" +
				"        self.crawl(response.url , callback=self.detail_page , save={\"title\":response.save[\"title\"]})\n" +
				"        \n" +
				"        for each_a in response.doc(\"a\").items():\n" +
				"            if filter(each_a.text()) == 1 and keyFilter(each_a.text()) == 1:\n" +
				"                self.crawl(each_a.attr.href , callback=self.index_page , save={\"title\":each_a.text()})\n" +
				"        \n" +
				"\n" +
				"    @config(age=10 * 24 * 60 * 60)\n" +
				"    def detail_page(self, response):\n" +
				"        result = {\n" +
				"            \"url\": response.url,\n" +
				"            \"title\": response.save['title'],\n" +
				"            \"text_info\": response.doc(\"body\").text()\n" +
				"        }\n" +
				"        html = \"<html><head><meta http-equiv='Content-Type' content='text/html;charset=utf-8'><base href='\"+response.url+\"'></head><body><div style='position:relative'>\"\n" +
				"        html += response.doc(\"HEAD\").html()\n" +
				"        html += response.doc(\"BODY\").html()\n" +
				"        html += \"</div></body></html>\"\n" +
				"        result[\"html\"] = html\n" +
				"        return result\n" +
				"    \n" +
				"    def on_result(self,result):\n" +
				"        if not result or not result[\"title\"]:\n" +
				"            return\n" +
				"        \n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        cur.execute(\"select * from baidu where url = %s\" , result[\"url\"])\n" +
				"        rows = cur.fetchall()\n" +
				"        if len(rows) == 0:\n" +
				"            cur.execute(\"select count(*) from baidu\") ;\n" +
				"            row = cur.fetchone()\n" +
				"            num = int(row[0]) + 1\n" +
				"            file_path = \"/home/mininet/quick_picture/\" + str(num) + \".html\"\n" +
				"            file = open(file_path,\"wt\")\n" +
				"            file.write(result[\"html\"])\n" +
				"            file.close()\n" +
				"            \n" +
				"            cur.execute(\"insert into baidu(url,title,text_info,quick_picture_file) values(%s,%s,%s,%s)\" , (result[\"url\"] , result[\"title\"],result[\"text_info\"],file_path))\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        \n" +
				"        \n" +
				"        \n" +
				"    \n" +
				"def filter(text):\n" +
				"    for word in filter_words:\n" +
				"        x = text.find(word)\n" +
				"        if x != -1:\n" +
				"            return 0\n" +
				"    return 1\n" +
				"\n" +
				"def keyFilter(text):\n" +
				"    for word in key_words:\n" +
				"        x = text.find(word)\n" +
				"        if x != -1:\n" +
				"            return 1\n" +
				"    return 0";
		public ArrayList<String> key_words = new ArrayList<String>(){{
			add("环境");add("污水");add("河长");add("水利");add("政策");
		}};
		public ArrayList<String> filter_words = new ArrayList<String>(){{
			add("广告");add("咨询");add("百度图片");add("公司");add("ppt");
		}};
	}

	public WeiXin WEIXIN = new WeiXin();
	class WeiXin{
		public String name = "weixin";
		public String script = "from pyspider.libs.base_handler import *\n" +
				"import datetime\n" +
				"import time\n" +
				"import json\n" +
				"import pymysql\n" +
				"reload(sys)\n" +
				"sys.setdefaultencoding('utf8')\n" +
				"\n" +
				"class Handler(BaseHandler):\n" +
				"    crawl_config = {\n" +
				"    }\n" +
				"\n" +
				"    @every(minutes=24 * 60 * 10)\n" +
				"    def on_start(self):\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        cur.execute(\"select * from weixin_public\")\n" +
				"        rows = cur.fetchall()\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        for row in rows:\n" +
				"            url = \"http://weixin.sogou.com/weixin?type=1&s_from=input&query=\"+row[1]+\"&ie=utf8&_sug_=n&_sug_type_=\"\n" +
				"            self.crawl(url, callback=self.index_page)\n" +
				"        \n" +
				"    @config(age=10 * 24 * 60 * 60)\n" +
				"    def index_page(self, response):\n" +
				"        for each in response.doc('UL.news-list2>LI:nth-child(1)>DIV.gzh-box2>DIV.txt-box>P.tit>a').items():\n" +
				"            self.crawl(each.attr.href, callback=self.list_page)\n" +
				"    \n" +
				"    @config(priority=3)\n" +
				"    def list_page(self, response):\n" +
				"        time.sleep(10)\n" +
				"        scriptStr = response.doc('body').text()\n" +
				"        if scriptStr.find(\"msgList\",1) != -1:\n" +
				"            start = scriptStr.find(\"msgList\",1) + 10\n" +
				"            end = len(scriptStr) - 42\n" +
				"            msg = json.loads(scriptStr[start:end])\n" +
				"            for each in msg[\"list\"]:\n" +
				"                url = each[\"app_msg_ext_info\"][\"content_url\"]\n" +
				"                url = url.replace(\"amp;\",\"\")\n" +
				"                self.crawl(\"https://mp.weixin.qq.com\" + url, callback=self.detail_page)\n" +
				"\n" +
				"    @config(priority=2)\n" +
				"    def detail_page(self, response):\n" +
				"        result = {\n" +
				"            \"title\": response.doc('h2#activity-name').text() ,\n" +
				"            \"time\": response.doc('em#post-date').text() ,\n" +
				"            \"public_signal\": response.doc('span.rich_media_meta.rich_media_meta_text.rich_media_meta_nickname').text() ,\n" +
				"            \"main_body\": response.doc('div#js_content').text() ,\n" +
				"            \"spider_time\" : time.strftime(\"%Y-%m-%d %H:%M:%S\", time.localtime()),\n" +
				"            \"url\": response.url,\n" +
				"        }\n" +
				"        return result\n" +
				"    \n" +
				"    def on_result(self,result):\n" +
				"        if not result or not result['title']:\n" +
				"            return\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        \n" +
				"        #先查找是否存在\n" +
				"        cur.execute(\"select * from weixin_info where title = %s\" , result[\"title\"])\n" +
				"        rows = cur.fetchall()\n" +
				"        if len(rows) == 0:\n" +
				"            cur.execute(\"insert into weixin_info(title,time,public_name,main_body,spider_time,url) values(%s,%s,%s,%s,%s,%s)\" , (result[\"title\"],result[\"time\"],result[\"public_signal\"],result[\"main_body\"],result[\"spider_time\"],result[\"url\"]))\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n";
	}

	public Zhejiang_Water ZHEJIANG_WATER = new Zhejiang_Water();
	class Zhejiang_Water{
		public String name = "zhejiang_water";
		public String script = "from pyspider.libs.base_handler import *\n" +
				"import datetime\n" +
				"import time\n" +
				"import pymysql\n" +
				"reload(sys)\n" +
				"sys.setdefaultencoding('utf8')\n" +
				"\n" +
				"class Handler(BaseHandler):\n" +
				"    \n" +
				"    \n" +
				"    \n" +
				"    crawl_config = {\n" +
				"        \"headers\":{\n" +
				"            \"Proxy-Connection\": \"keep-alive\",\n" +
				"            \"Pragma\": \"no-cache\",\n" +
				"            \"Cache-Control\": \"no-cache\",\n" +
				"            \"User-Agent\": \"Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36\",\n" +
				"            \"Accept\": \"*/*\",\n" +
				"            \"DNT\": \"1\",\n" +
				"            \"Accept-Encoding\": \"gzip, deflate, sdch\",\n" +
				"            \"Accept-Language\": \"zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4\",\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    @every(minutes=72 * 60)\n" +
				"    def on_start(self):\n" +
				"        \n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        cur.execute(\"select * from web_list where web_name = %s\" , '浙江水利厅')\n" +
				"        rows = cur.fetchall()\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"        \n" +
				"        for row in rows:\n" +
				"            url = row[2]\n" +
				"            page_type = row[3]\n" +
				"            page_num =  row[4]\n" +
				"            for i in range(1,int(page_num)+1):\n" +
				"                page_url = url + str(i) + \".htm\"\n" +
				"                self.crawl(page_url , callback=self.new_list_page , save={\"page_type\":page_type})\n" +
				"            \n" +
				"\n" +
				"    @config(age=10 * 24 * 60 * 60)\n" +
				"    def new_list_page(self, response):\n" +
				"        page_type = response.save[\"page_type\"]\n" +
				"        #获取每个新闻的title和time信息,url\n" +
				"        count = 0\n" +
				"        for each_line_tr in response.doc('table#ctl00_cphBody_ctl00_dt_itemlist>tr').items():\n" +
				"            count += 1\n" +
				"            if count == 1:\n" +
				"                continue\n" +
				"            for each_new_table in each_line_tr('table').items():\n" +
				"                title = each_new_table('div.SHORT_DIV1>a').text()\n" +
				"                href = each_new_table('div.SHORT_DIV1>a').attr.href\n" +
				"                time = each_new_table('tr>td:nth-child(2)').text()\n" +
				"                self.crawl(href , callback=self.detail_page , save={\"title\":title,\"time\":time,\"page_type\":page_type})\n" +
				"    \n" +
				"    @config()\n" +
				"    def index_page(self, response):\n" +
				"        for each in response.doc('a[href^=\"http\"]').items():\n" +
				"            self.crawl(each.attr.href, callback=self.detail_page)\n" +
				"\n" +
				"    @config(priority=2)\n" +
				"    def detail_page(self, response):\n" +
				"        artical = \"\"\n" +
				"        for each_word in response.doc('td#NewsContent').items():\n" +
				"            for each_p in each_word('p').items():\n" +
				"                artical += each_p.text()\n" +
				"         \n" +
				"        come_from = response.doc('td#NewsContent>div').text()\n" +
				"        artical_set = set(artical)\n" +
				"        if len(artical_set) == 0:\n" +
				"            artical =  \"\"\n" +
				"            for each_p in response.doc('p').items():\n" +
				"                artical += each_p.text()\n" +
				"            come_from =  \"本站\"\n" +
				"            artical_set = set(artical)\n" +
				"        if len(artical_set) == 0:\n" +
				"            title = response.save[\"title\"]\n" +
				"            push_time = response.save[\"time\"]\n" +
				"            page_type = response.save[\"page_type\"]\n" +
				"            url = \"http://www.zjwater.com\" + response.doc(\"HTML\").text()[17:-2]\n" +
				"            self.crawl(url , callback=self.detail_page , save={\"title\":title,\"time\":push_time,\"page_type\":page_type})\n" +
				"        else:\n" +
				"            for ab in artical_set:\n" +
				"                if ab in 'qwertyuiopasdfghjklzxcvbnm/= ':\n" +
				"                    artical = artical.replace(ab,'')\n" +
				"            result = {}\n" +
				"            result[\"url\"] = response.url\n" +
				"            result[\"title\"] = response.save[\"title\"]\n" +
				"            result[\"push_time\"] = response.save[\"time\"]\n" +
				"            result[\"context\"] = artical\n" +
				"            result[\"spider_time\"] = datetime.datetime.now()\n" +
				"            result[\"come_from\"] = come_from\n" +
				"            result[\"page_type\"] = response.save[\"page_type\"]\n" +
				"            return result\n" +
				"    \n" +
				"    def on_result(self,result):\n" +
				"        if not result or not result['title']:\n" +
				"            return\n" +
				"        conn= pymysql.connect(host=host,port=port,user=user,passwd=passwd,db=db,charset='utf8')\n" +
				"        cur = conn.cursor()\n" +
				"        \n" +
				"        #先查找是否存在\n" +
				"        cur.execute(\"select * from zhejiang_water where url = %s\" , result[\"url\"])\n" +
				"        rows = cur.fetchall()\n" +
				"        if len(rows) == 0:\n" +
				"            cur.execute(\"insert into zhejiang_water(title,url,push_time,spider_time,come_from,context,page_type) values(%s,%s,%s,%s,%s,%s,%s)\" , (result[\"title\"],result[\"url\"],result[\"push_time\"],result[\"spider_time\"],result[\"come_from\"],result[\"context\"],result[\"page_type\"]))\n" +
				"        conn.commit()\n" +
				"        cur.close()\n" +
				"        conn.close()\n" +
				"\n";
	}

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
	class Project{
		public String host;
		public int port;
		public String user;
		public String passwd;
		public String db;
		public String template;
		Project(String host,int port,String user,String passwd,String db){
			this.host = host;
			this.port = port;
			this.user = user;
			this.passwd = passwd;
			this.db = db;
		}
		public String getScript(String template,ArrayList<String> key_words,ArrayList<String> filter_words){
			this.template = template;
			String result = "key_words=[";
			for(String word:key_words){
				result+="'"+word+"',";
			}
			result+="]\n";
			result+="filter_words=[";
			for(String word:filter_words){
				result+="'"+word+"',";
			}
			result+="]\n";
			result = result.replace(",]","]");
			result+= "host='"+this.host+"'\nport="+this.port+"\nuser='"+this.user+"'\npasswd='"+this.passwd+"'\ndb='"+this.db+"'\n"+this.template;
			return result;
		}
	}
	
	PySpider(){
	}
	PySpider(String host){
		this.HOST = host;
	}
	PySpider(int port){
		this.SERVER_PORT = port;
	}
	PySpider(String host,int port){
		this.HOST = host;
		this.SERVER_PORT = port;
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
		Socket socket = new Socket(addr, this.SERVER_PORT);
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
	 *
	 * @param name
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public String createProject(String name,String type)throws IOException{
		Project project = new Project(this.HOST,this.DB_PORT,this.user,this.passwd,this.db);
		String result = new String();
		this.Current_Type = type;
		switch (type){
			case "wanfang":
				result = this.saveScript(name+"_1",project.getScript(this.WANFANG.spider_script,this.WANFANG.key_words,new ArrayList<>()));
				result+=","+this.saveScript(name+"_2",project.getScript(this.WANFANG.paper_script,new ArrayList<>(),new ArrayList<>()));
				break;
			case "19lou":
				result = this.saveScript(name,project.getScript(this._19LOU.script,new ArrayList<>(),new ArrayList<>()));
				break;
			case "baidu":
				result = this.saveScript(name,project.getScript(this.BAIDU.script,this.BAIDU.key_words,this.BAIDU.filter_words));
				break;
			case "weixin":
				result = this.saveScript(name,project.getScript(this.WEIXIN.script,new ArrayList<String>(),new ArrayList<>()));
				break;
			case "zhejiang_water":
				result = this.saveScript(name,project.getScript(this.ZHEJIANG_WATER.script,new ArrayList<>(),new ArrayList<>()));
				break;
			default:
				result = "please select type";
				break;
		}
		return this.generateResult("create"+type,"String",result);
	}

	public String createProject(String name,String type,ArrayList<String> key_words,ArrayList<String> filter_words)throws IOException{
		Project project = new Project(this.HOST,this.DB_PORT,this.user,this.passwd,this.db);
		String result = new String();
		this.Current_Type = type;
		switch (type){
			case "wanfang":
				result = this.saveScript(name+"_1",project.getScript(this.WANFANG.spider_script,key_words,filter_words));
				result+=","+this.saveScript(name+"_2",project.getScript(this.WANFANG.paper_script,key_words,key_words));
				break;
			case "19lou":
				result = this.saveScript(name,project.getScript(this._19LOU.script,key_words,filter_words));
				break;
			case "baidu":
				result = this.saveScript(name,project.getScript(this.BAIDU.script,key_words,filter_words));
				break;
			case "weixin":
				result = this.saveScript(name,project.getScript(this.WEIXIN.script,key_words,filter_words));
				break;
			case "zhejiang_water":
				result = this.saveScript(name,project.getScript(this.ZHEJIANG_WATER.script,key_words,filter_words));
				break;
			default:
				result = "please select type";
				break;
		}
		return this.generateResult("create"+type,"String",result);
	}

	/**
	 *
	 * @param project
	 * @param key_words
	 * @param filter_words
	 * @return
	 * @throws IOException
	 */
	public String updateProject(String project,ArrayList<String> key_words,ArrayList<String> filter_words)throws IOException{
		return this.generateResult("update","string",this.createProject(project,this.Current_Type,key_words,filter_words));
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