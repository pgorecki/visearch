<?php

namespace Search\Model;

use Zend\Db\TableGateway\TableGateway;
use Zend\Db\Sql\Select;
use Zend\Db\Sql\Sql;
use Zend\ServiceManager\ServiceLocatorAwareInterface;
use Zend\ServiceManager\ServiceLocatorInterface;
use Zend\Db\Adapter\Adapter;


//class DBManager implements ServiceLocatorAwareInterface {
	
class SearchDBManager {
	
	private $serviceLocator;
	
	private $db;
	
	
	
	public function __construct(Adapter $dbAdapter) {
		$this->db = $dbAdapter;
	}
	
	
	
	public function getImgRepresentation($id)
	{
		//$this->db = $this->getServiceLocator()->get('Zend\Db\Adapter\Adapter');
	
		$sql = new Sql($this->db);
		$select = $sql->select();
		$select->from('ImageRepresentations');
		$select->where(array('ImageId' => $id));
		
		$statement = $sql->prepareStatementForSqlObject($select);
		$result = $statement->execute();
		
		
		//$result->next();
		$imgRep = $result->current();
		
		return $imgRep;
		
	}
	
	/*
	public function setServiceLocator(ServiceLocatorInterface $serviceLocator) {
		$this->serviceLocator = $serviceLocator;
	}
	public function getServiceLocator() {
		return $this->serviceLocator;
	}
	*/
}

?>