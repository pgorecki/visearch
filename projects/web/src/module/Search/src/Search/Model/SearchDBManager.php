<?php

namespace Search\Model;

use Zend\Db\TableGateway\TableGateway;
use Zend\Db\Sql\Select;
use Zend\Db\Sql\Sql;
use Zend\ServiceManager\ServiceLocatorAwareInterface;
use Zend\ServiceManager\ServiceLocatorInterface;
use Zend\Db\Adapter\Adapter;
use Zend\Db\Sql\Where;


//class DBManager implements ServiceLocatorAwareInterface {
	
class SearchDBManager {
	
	private $serviceLocator;
	
	
	/**
	 * 
	 * @var Zend\Db\Adapter\Adapter
	 */
	private $db;
	
	
	
	public function __construct(Adapter $dbAdapter) {
		$this->db = $dbAdapter;
	}
	
	
	/**
	 * Function get the image representation string from tabel ImageRepresentation
	 * @param int $id - image id
	 * @return string  -  image representation in sparse format
	 */
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
	
	
	/**
	 * 
	 * @param array $vw - asociative array with visual word numbers and
	 */
	public function getRankingCandidates($vw, $minNumVW=10) {
	
	   //SELECT COUNT(*), ImageId FROM `IFS` WHERE `VisualWord` 
	   //IN (3,5,4,6,108, 160, 577,1261,1427) GROUP BY ImageId ORDER BY 1 DESC
		
		$adapter= $this->db;
		//$adapter->query('SELECT COUNT(*), ImageId FROM `IFS` WHERE `VisualWord` IN (?) GROUP BY ImageId ORDER BY 1 DESC ', $vw);
		
		$t = join(',', $vw);		

		$numVW = array($minNumVW);
		$statement = $adapter->createStatement('SELECT COUNT(*) as numVW, ImageId FROM `IFS` WHERE `VisualWord` in ('.$t.') GROUP BY ImageId HAVING numVW>? ORDER BY 1 DESC ',$numVW);

		$result = $statement->execute();
		
		$imgId = array();
		foreach ($result as $row) {
			$imgId[$row['ImageId']]=$row['numVW'];
		}
		
		return  $imgId;
		
	}
	
	
	
	public function getVisualWordsFromRep($imgRep){
		
		//regexp
		$pattern = '/(\d+):(\d+.?\d*)/';
		preg_match_all($pattern, $imgRep, $matches);
		
		$vw=$matches[1];
		sort($vw);
		
		
		//json format
		//$picRep ='{"1":5,"2":6}';
		//$vw= json_decode($picRep,true);
		
		return $vw;
		
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