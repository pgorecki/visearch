-- phpMyAdmin SQL Dump
-- version 3.4.10.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Sep 12, 2013 at 01:29 PM
-- Server version: 5.5.32
-- PHP Version: 5.3.10-1ubuntu3.7

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `VisualSearchDB`
--

-- --------------------------------------------------------

--
-- Table structure for table `IFS`
--

DROP TABLE IF EXISTS `IFS`;
CREATE TABLE IF NOT EXISTS `IFS` (
  `VisualWord` int(11) NOT NULL,
  `ImageId` bigint(20) NOT NULL,
  PRIMARY KEY (`VisualWord`,`ImageId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `ImageDescriptors`
--

DROP TABLE IF EXISTS `ImageDescriptors`;
CREATE TABLE IF NOT EXISTS `ImageDescriptors` (
  `ImageId` bigint(20) NOT NULL DEFAULT '0',
  `descriptor` varchar(10) NOT NULL,
  `creation_date` date NOT NULL,
  `settings_path` varchar(255) NOT NULL,
  `descriptor_file_path` varchar(255) NOT NULL,
  PRIMARY KEY (`ImageId`,`descriptor`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `ImageRepresentations`
--

DROP TABLE IF EXISTS `ImageRepresentations`;
CREATE TABLE IF NOT EXISTS `ImageRepresentations` (
  `ImageId` bigint(20) NOT NULL,
  `Representation` text NOT NULL,
  PRIMARY KEY (`ImageId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `Images`
--

DROP TABLE IF EXISTS `Images`;
CREATE TABLE IF NOT EXISTS `Images` (
  `ImageId` bigint(20) NOT NULL AUTO_INCREMENT,
  `FileName` varchar(128) NOT NULL,
  `FileDirectory` varchar(128) NOT NULL,
  `URL` varchar(1024) NOT NULL,
  `TimeStamp` int(11) NOT NULL,
  PRIMARY KEY (`ImageId`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=256 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
