--
-- Create schema tandembrowsing
--

CREATE DATABASE IF NOT EXISTS tandembrowsing;
USE tandembrowsing;

--
-- Definition of table `cell_state`
--
DROP TABLE IF EXISTS `virtualscreens`;
CREATE TABLE `virtualscreens` (
  `insertion_order` int NOT NULL AUTO_INCREMENT,
  `session` varchar(767) NOT NULL,
  `id` varchar(255) NOT NULL,
  `resource` varchar(1023) NOT NULL,
  `browser` varchar(255) default NULL,
  `width` float default NULL,
  `height` float default NULL,
  `xPosition` float default NULL,
  `yPosition` float default NULL,
  `zIndex` float default NULL,
  `border` float default NULL,
  `resizable` boolean default NULL,
  PRIMARY KEY  (`insertion_order`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DROP TABLE IF EXISTS `statemachines`;
CREATE TABLE `statemachines` (
  `session` varchar(767) NOT NULL,
  `url` varchar(1023) NOT NULL,
  `state` varchar(255) default NULL,
  PRIMARY KEY (`session`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;