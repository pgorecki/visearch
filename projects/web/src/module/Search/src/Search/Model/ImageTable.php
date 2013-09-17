<?php

namespace Search\Model;

use Zend\Db\TableGateway\TableGateway;
use Zend\Db\Sql\Select;
use Zend\ServiceManager\ServiceLocatorAwareInterface;
use Zend\ServiceManager\ServiceLocatorInterface;

class ImageTable implements ServiceLocatorAwareInterface {
	protected $serviceLocator;
	protected $tableGateway;
	public function __construct(TableGateway $tableGateway) {
		$this->tableGateway = $tableGateway;
	}
	public function fetchAll() {
		// $resultSet = $this->tableGateway->select();
		// return $resultSet;
		$imageTable = $this->tableGateway;
		
		$resultSet = $imageTable->select ( function (Select $select) {
			$select->limit ( 10 );
		} );
		
		
		$folerDir = '/pics/';
		
		$resultSet->buffer ();
		
		$images = array();
		
		foreach ($resultSet as $img)
		{
			$img->imagePath = $folerDir.$img->fileDirectory.'/'.$img->fileName;
			$images[] = $img;	
		}
		
		
		
		return $images;
		//return $resultSet;
	}
	public function getImage($id) {
		$id = ( int ) $id;
		$rowset = $this->tableGateway->select ( array (
				'ImageId' => $id 
		) );
		$row = $rowset->current ();
		if (! $row) {
			throw new \Exception ( "Could not find row $id" );
		}
		return $row;
	}
	public function saveImage(Image $image) {
	}
	public function deleteImage($id) {
		$this->tableGateway->delete ( array (
				'ImageId' => $id 
		) );
	}
	public function setServiceLocator(ServiceLocatorInterface $serviceLocator) {
		$this->serviceLocator = $serviceLocator;
	}
	public function getServiceLocator() {
		return $this->serviceLocator;
	}
}
