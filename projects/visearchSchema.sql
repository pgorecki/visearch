-- phpMyAdmin SQL Dump
-- version 3.5.8.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Czas wygenerowania: 17 Wrz 2013, 14:14
-- Wersja serwera: 5.5.32-0ubuntu0.13.04.1
-- Wersja PHP: 5.4.9-4ubuntu2.3

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Baza danych: `VisualSearchDB`
--

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `IFS`
--

CREATE TABLE IF NOT EXISTS `IFS` (
  `VisualWord` int(11) NOT NULL,
  `ImageId` bigint(20) NOT NULL,
  PRIMARY KEY (`VisualWord`,`ImageId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `ImageDescriptors`
--

CREATE TABLE IF NOT EXISTS `ImageDescriptors` (
  `ImageId` bigint(20) NOT NULL DEFAULT '0',
  `Descriptor` varchar(10) NOT NULL,
  `Created` datetime NOT NULL,
  `DescriptorsPath` varchar(255) NOT NULL,
  PRIMARY KEY (`ImageId`,`Descriptor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `ImageRepresentations`
--

CREATE TABLE IF NOT EXISTS `ImageRepresentations` (
  `ImageId` bigint(20) NOT NULL,
  `Representation` text NOT NULL,
  PRIMARY KEY (`ImageId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `Images`
--

CREATE TABLE IF NOT EXISTS `Images` (
  `ImageId` bigint(20) NOT NULL AUTO_INCREMENT,
  `FileName` varchar(128) NOT NULL,
  `FileDirectory` varchar(128) NOT NULL,
  `URL` varchar(1024) NOT NULL,
  `Created` datetime NOT NULL,
  PRIMARY KEY (`ImageId`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1027 ;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
