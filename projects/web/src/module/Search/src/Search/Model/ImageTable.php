<?php

namespace Search\Model;

use Zend\Db\TableGateway\TableGateway;
use Zend\Db\Sql\Select;
use Zend\ServiceManager\ServiceLocatorAwareInterface;
use Zend\ServiceManager\ServiceLocatorInterface;
use Zend\Db\Sql\Where;

class ImageTable implements ServiceLocatorAwareInterface {
	protected $serviceLocator;
	protected $tableGateway;
	public function __construct(TableGateway $tableGateway) {
		$this->tableGateway = $tableGateway;
	}
	public function getImgRepresentation($id) {
	}
	public function fetchAll() {
		// $resultSet = $this->tableGateway->select();
		// return $resultSet;
		$imageTable = $this->tableGateway;
		
		$resultSet = $imageTable->select ( function (Select $select) {
			$select->limit (100 );
		} );
		
		$resultSet->buffer ();
		
		$images = array ();
		
		foreach ( $resultSet as $img ) {
			// $img->imagePath = $folerDir.$img->fileDirectory.$img->fileName;
			
			$img = $this->setImagepath ( $img );
			$images [] = $img;
		}
		
		return $images;
		// return $resultSet;
	}
	public function fetchImages($imageIds) {
		// $resultSet = $this->tableGateway->select();
		// return $resultSet;
		$imageTable = $this->tableGateway;
		
		
		//this doesn't work, $imageIds are undefined
		//but when we write ids explicte as 'array(1,2,3)' than it works 
		 /* $resultSet = $imageTable->select ( function (Select $select) { 
		  		$select->where->in('ImageId',$imageIds ); 
		 } );
		 */
		
		$where = new Where ();
		$where->in ( "ImageId", $imageIds );
		$resultSet = $imageTable->select ( $where );
		
		$resultSet->buffer ();
		
		$images = array ();
		
		foreach ( $resultSet as $img ) {
			// $img->imagePath = $folerDir.$img->fileDirectory.$img->fileName;
			
			$img = $this->setImagepath ( $img );
			$images [] = $img;
		}
		
		return $images;
		// return $resultSet;
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
		$row = $this->setImagePath ( $row );
		
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
	protected function setImagePath($img) {
		$folerDir = '/pics/';
		
		$img->imagePath = $folerDir . $img->fileDirectory.$img->fileName;
		return $img;
	}
	
}
