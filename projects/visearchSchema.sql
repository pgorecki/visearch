-- phpMyAdmin SQL Dump
-- version 3.5.8.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Czas wygenerowania: 10 Wrz 2013, 14:07
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
  `visualWord` int(11) NOT NULL,
  `imageId` varchar(64) NOT NULL,
  PRIMARY KEY (`visualWord`,`imageId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `ImageDescriptors`
--

CREATE TABLE IF NOT EXISTS `ImageDescriptors` (
  `FileName` varchar(128) NOT NULL DEFAULT '',
  `descriptor` varchar(10) NOT NULL,
  `creation_date` date NOT NULL,
  `settings_path` varchar(255) NOT NULL,
  `descriptor_file_path` varchar(255) NOT NULL,
  PRIMARY KEY (`FileName`,`descriptor`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `ImageRepresentations`
--

CREATE TABLE IF NOT EXISTS `ImageRepresentations` (
  `imageId` varchar(64) NOT NULL,
  `representation` text NOT NULL,
  PRIMARY KEY (`imageId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struktura tabeli dla tabeli `Images`
--

CREATE TABLE IF NOT EXISTS `Images` (
  `FileName` varchar(128) NOT NULL DEFAULT '',
  `FileDirectory` varchar(128) NOT NULL,
  `URL` varchar(1024) NOT NULL,
  `TimeStamp` int(11) NOT NULL,
  PRIMARY KEY (`FileName`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
