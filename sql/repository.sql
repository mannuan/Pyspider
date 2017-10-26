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

insert into key_word(key_word) values('污水治理'),('一河一策'),('水政执法'),('河长制六大任务'),('水资源保护'),('河湖水域岸线保护'),('水污染防治'),('水环境治理'),('污染源类型'),('水生态修复'),('水文化'),('水安全'),('水利工程管理'),('环境监督'),('黑臭水治理'),('河道治理'),('河流治理'),('水质监测');

