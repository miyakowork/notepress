

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for np_category
-- ----------------------------
DROP TABLE IF EXISTS `np_category`;
CREATE TABLE `np_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pid` bigint(20) DEFAULT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `font_icon` varchar(50) DEFAULT NULL,
  `img_icon` varchar(255) DEFAULT NULL,
  `order_index` int(11) DEFAULT NULL,
  `status` tinyint(1) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_content
-- ----------------------------
DROP TABLE IF EXISTS `np_content`;
CREATE TABLE `np_content` (
  `id` varchar(50) NOT NULL,
  `author_id` bigint(20) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `url_seq` varchar(255) DEFAULT NULL,
  `cover` varchar(255) DEFAULT NULL,
  `images` text,
  `html_content` longtext,
  `text_content` longtext,
  `md_content` longtext,
  `seo_keywords` varchar(255) DEFAULT NULL,
  `seo_description` varchar(255) DEFAULT NULL,
  `appreciable` tinyint(1) DEFAULT NULL,
  `reprinted` tinyint(1) DEFAULT NULL,
  `origin_url` varchar(255) DEFAULT NULL,
  `history` tinyint(1) DEFAULT NULL,
  `views` int(11) DEFAULT NULL,
  `approve_cnt` int(11) DEFAULT NULL,
  `commented` tinyint(1) DEFAULT NULL,
  `top` tinyint(1) DEFAULT NULL,
  `recommend` tinyint(1) DEFAULT NULL,
  `hot` tinyint(1) DEFAULT NULL,
  `visible` tinyint(1) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_deal
-- ----------------------------
DROP TABLE IF EXISTS `np_deal`;
CREATE TABLE `np_deal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `deal_target_id` varchar(255) DEFAULT NULL COMMENT '???????????????id',
  `deal_amount` double(20,0) DEFAULT NULL COMMENT '????????????????????????',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_dictionary
-- ----------------------------
DROP TABLE IF EXISTS `np_dictionary`;
CREATE TABLE `np_dictionary` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dict_label` varchar(50) DEFAULT NULL,
  `dict_value` varchar(50) DEFAULT NULL,
  `status` tinyint(1) DEFAULT '1',
  `is_default` tinyint(1) DEFAULT '0',
  `dictionary_type` varchar(50) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_hide
-- ----------------------------
DROP TABLE IF EXISTS `np_hide`;
CREATE TABLE `np_hide` (
  `id` varchar(100) NOT NULL,
  `content_id` varchar(100) NOT NULL,
  `hide_type` varchar(50) DEFAULT NULL,
  `hide_price` int(20) DEFAULT NULL,
  `hide_html` text,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_oauth
-- ----------------------------
DROP TABLE IF EXISTS `np_oauth`;
CREATE TABLE `np_oauth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT ' ',
  `oauth_type` varchar(50) NOT NULL,
  `client_id` varchar(255) NOT NULL,
  `client_secret` varchar(255) NOT NULL,
  `redirect_uri` varchar(255) DEFAULT NULL,
  `extra_param` text COMMENT '??? json ???????????????????????????',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_param
-- ----------------------------
DROP TABLE IF EXISTS `np_param`;
CREATE TABLE `np_param` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `value` text,
  `group` int(11) DEFAULT NULL,
  `order_index` int(11) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_pay_order
-- ----------------------------
DROP TABLE IF EXISTS `np_pay_order`;
CREATE TABLE `np_pay_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) DEFAULT NULL,
  `order_type` varchar(50) NOT NULL DEFAULT 'wechat',
  `order_price` double(10,2) NOT NULL DEFAULT '0.00',
  `order_name` varchar(255) NOT NULL,
  `pay_status` tinyint(4) DEFAULT NULL COMMENT '0????????????1????????????-1?????????',
  `expired` datetime NOT NULL COMMENT '??????????????????',
  `extra_json` text COMMENT '????????????????????????json??????',
  `gmt_create` datetime DEFAULT NULL COMMENT '??????????????????',
  `gmt_update` datetime DEFAULT NULL COMMENT '????????????????????????????????????????????????',
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_pay_qrcode
-- ----------------------------
DROP TABLE IF EXISTS `np_pay_qrcode`;
CREATE TABLE `np_pay_qrcode` (
  `id` varchar(50) NOT NULL,
  `qr_type` varchar(20) NOT NULL,
  `qr_url` varchar(255) NOT NULL,
  `qr_price` double(10,2) NOT NULL DEFAULT '0.00',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`qr_type`,`qr_price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_refer
-- ----------------------------
DROP TABLE IF EXISTS `np_refer`;
CREATE TABLE `np_refer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `self_id` varchar(255) DEFAULT NULL COMMENT '??????id????????????????????????content_id',
  `refer_id` varchar(255) DEFAULT NULL COMMENT '?????????id',
  `refer_type` varchar(255) DEFAULT NULL COMMENT '??????????????????????????????????????????????????????comment_user,nameself_id ???comment_id???refer_id???user_id',
  `refer_extra` text COMMENT '???????????????????????????????????????{key:value}',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=423 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_res
-- ----------------------------
DROP TABLE IF EXISTS `np_res`;
CREATE TABLE `np_res` (
  `id` varchar(50) NOT NULL,
  `res_hash` varchar(255) NOT NULL COMMENT '???????????????????????????????????????????????????????????????????????????',
  `res_url` longtext NOT NULL,
  `res_intro_url` text,
  `auth_code` varchar(50) DEFAULT NULL,
  `res_fsize_bytes` double DEFAULT '0' COMMENT '???????????? ?????????B',
  `coin` int(11) NOT NULL DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_res_cate
-- ----------------------------
DROP TABLE IF EXISTS `np_res_cate`;
CREATE TABLE `np_res_cate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pid` bigint(20) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `order_index` int(11) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_sys_log
-- ----------------------------
DROP TABLE IF EXISTS `np_sys_log`;
CREATE TABLE `np_sys_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content_type` varchar(255) DEFAULT NULL,
  `ip_addr` varchar(255) DEFAULT NULL,
  `ip_info` varchar(255) DEFAULT NULL,
  `request_method` varchar(255) DEFAULT NULL,
  `session_id` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `user_agent` text,
  `username` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `browser` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  FULLTEXT KEY `ip_info` (`ip_info`)
) ENGINE=InnoDB AUTO_INCREMENT=6094 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `np_sys_notice`;
CREATE TABLE `np_sys_notice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content_id` varchar(100) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `reply_id` bigint(20) DEFAULT NULL COMMENT '???????????????ID?????????????????????????????????????????????',
  `comment_text` varchar(1000) DEFAULT NULL,
  `comment_html` varchar(1000) DEFAULT NULL,
  `status` tinyint(1) NOT NULL,
  `ip_addr` varchar(50) DEFAULT NULL,
  `ip_info` varchar(100) DEFAULT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `floor` int(11) NOT NULL DEFAULT '-1',
  `is_read` tinyint(1) DEFAULT '0',
  `page_type` varchar(50) DEFAULT NULL COMMENT '????????????????????????????????????????????????',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_sys_session
-- ----------------------------
DROP TABLE IF EXISTS `np_sys_session`;
CREATE TABLE `np_sys_session` (
  `jwt_token` text,
  `session_user_id` bigint(20) DEFAULT NULL,
  `session_username` varchar(20) DEFAULT NULL,
  `expired` datetime NOT NULL,
  `admin_req` tinyint(1) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_sys_user
-- ----------------------------
DROP TABLE IF EXISTS `np_sys_user`;
CREATE TABLE `np_sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) DEFAULT NULL,
  `email` varbinary(255) DEFAULT NULL,
  `status` tinyint(1) NOT NULL,
  `admin` tinyint(1) NOT NULL DEFAULT '0' COMMENT '??????????????????',
  `nickname` varchar(50) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(20) DEFAULT NULL,
  `last_login_ip` varchar(50) DEFAULT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `last_login_addr` varchar(100) DEFAULT NULL,
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for np_upload
-- ----------------------------
DROP TABLE IF EXISTS `np_upload`;
CREATE TABLE `np_upload` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `disk_path` varchar(255) NOT NULL,
  `type` varchar(100) NOT NULL,
  `upload` datetime(6) DEFAULT NULL,
  `virtual_path` varchar(255) NOT NULL,
  `content_type` varchar(255) DEFAULT NULL,
  `object_key_id` varchar(50) DEFAULT NULL COMMENT '??????id???????????????????????????id',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_update` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL COMMENT '?????????????????????????????????????????????',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;
