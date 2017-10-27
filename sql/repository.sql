CREATE DATABASE `repository`; /*!40100 DEFAULT CHARACTER SET utf8 */
use repository;

//爬取百度数据的数据库脚本
DROP TABLE IF EXISTS `baidu`;
CREATE TABLE `baidu` (
  `id` int(5) NOT NULL AUTO_INCREMENT,
  `url` text NOT NULL,
  `text_info` text NOT NULL,
  `quick_picture_file` varchar(100) NOT NULL,
  `indexed` int(1) NOT NULL DEFAULT '0',
  `title` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `key_word`;
CREATE TABLE `key_word` (
  `id` int(5) NOT NULL AUTO_INCREMENT,
  `key_word` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
INSERT INTO key_word(key_word) VALUES ('污水治理'),('一河一策'),('水政执法'),('河长制六大任务'),('水资源保护'),('河湖水域岸线保护'),('水污染防治'),('水环境治理'),('污染源类型'),('水生态修复'),('水文化'),('水安全'),('水利工程管理'),('环境监督'),('黑臭水治理'),('河道治理'),('河流治理'),('水质监测');

//爬取杭州19楼的数据库脚本
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note` (
  `note_id` int(10) NOT NULL AUTO_INCREMENT,
  `note_title` text NOT NULL,
  `note_url` text NOT NULL,
  `note_context` text,
  `note_push_time` varchar(20) NOT NULL,
  `note_update_time` varchar(20) NOT NULL,
  `note_spider_time` varchar(20) NOT NULL,
  `note_push_person_id` int(10) NOT NULL,
  `note_audit` int(1) DEFAULT '0',
  `note_update` int(1) DEFAULT '1',
  PRIMARY KEY (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `author`;
CREATE TABLE `author` (
  `author_id` int(10) NOT NULL AUTO_INCREMENT,
  `author_url` varchar(50) NOT NULL,
  `author_name` varchar(100) NOT NULL,
  `author_city` varchar(50) DEFAULT NULL,
  `sex` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `note_comment`;
CREATE TABLE `note_comment` (
  `note_id` int(10) NOT NULL,
  `comment_id` int(10) NOT NULL,
  `comment_context` text NOT NULL,
  `comment_push_time` varchar(20) NOT NULL,
  `comment_spider_time` varchar(20) NOT NULL,
  `comment_push_person_id` int(10) NOT NULL,
  PRIMARY KEY (`note_id`,`comment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `note_trend`;
CREATE TABLE `note_trend` (
  `note_id` int(10) NOT NULL,
  `count_time` varchar(20) NOT NULL,
  `look_num` int(10) DEFAULT NULL,
  `comment_num` int(10) DEFAULT NULL,
  `hot` int(10) DEFAULT NULL,
  PRIMARY KEY (`note_id`,`count_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

//爬取万方论文的数据库脚本
DROP TABLE IF EXISTS `wanfang`;
CREATE TABLE `wanfang` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '主键自增长',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `abstract` text NOT NULL COMMENT '摘要',
  `author` text NOT NULL COMMENT '作者(中文+英文可能有)',
  `unit` text NOT NULL COMMENT '工作单位',
  `magezine` varchar(200) NOT NULL COMMENT '发表的期刊',
  `file_url` varchar(200) NOT NULL COMMENT '论文url',
  `time` varchar(50) NOT NULL COMMENT '发表时间',
  `keyword` text NOT NULL COMMENT '关键字',
  `url` varchar(100) NOT NULL COMMENT '页面url',
  `spider_time` varchar(50) NOT NULL COMMENT '爬取时间',
  `paper_flag` int(1) NOT NULL DEFAULT '0' COMMENT '论文pdf是否爬取',
  `indexed` int(1) NOT NULL DEFAULT '0',
  `file_path` varchar(100) DEFAULT NULL,
  `transform_flag` int(1) NOT NULL DEFAULT '0',
  `transform_text_path` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6216 DEFAULT CHARSET=utf8;

//爬取微信数据的数据库脚本
DROP TABLE IF EXISTS `weixin_public`;
CREATE TABLE `weixin_public` (
  `id` int(5) NOT NULL AUTO_INCREMENT,
  `public_name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `weixin_info`;
CREATE TABLE `weixin_info` (
  `id` int(5) NOT NULL AUTO_INCREMENT,
  `title` text NOT NULL,
  `time` varchar(50) NOT NULL,
  `public_name` varchar(100) NOT NULL,
  `main_body` text NOT NULL,
  `spider_time` varchar(100) NOT NULL,
  `indexed` int(1) NOT NULL DEFAULT '0',
  `url` text,
  `type_id` int(5) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO weixin_public(public_name) VALUES ('中国河长制'),('粤水利人'),('福建河长'),('西安河长'),('钱塘江河长'),('贵州河长'),('重庆河长制'),('湖南省河长制'),('环境修复论坛'),('水利家园'),('武义县五水共治 '),('中国环境新闻');

//爬取浙江水利厅数据的数据库脚本
DROP TABLE IF EXISTS `web_list`;
CREATE TABLE `web_list` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `web_name` varchar(20) NOT NULL,
  `web_url` varchar(200) NOT NULL,
  `web_type` varchar(50) NOT NULL,
  `page_number` int(3) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO web_list(web_name,web_url,web_type,page_number) VALUES ('浙江水利厅','http://www.zjwater.com/pages/category/0b50/index_','河长制工作动态',1),('浙江水利厅','http://www.zjwater.com/pages/category/0b53/index_','河长制媒体聚焦',1),('浙江水利厅','http://www.zjwater.com/pages/category/0b54/index_','河长制工作方案',1),('浙江水利厅','http://www.zjwater.com/pages/category/032/index_','浙江水利要闻',70),('浙江水利厅','http://www.zjwater.com/pages/category/0bj9/index_','浙江水利经验交流',1);

DROP TABLE IF EXISTS `zhejiang_water`;
CREATE TABLE `zhejiang_water` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `url` varchar(100) NOT NULL,
  `push_time` varchar(50) NOT NULL,
  `spider_time` varchar(50) NOT NULL,
  `come_from` text NOT NULL,
  `context` text NOT NULL,
  `indexed` int(1) DEFAULT '0',
  `page_type` varchar(50) NOT NULL,
  `type_id` int(5) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


