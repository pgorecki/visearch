<?php

namespace Search\Model;

use Zend\Db\TableGateway\TableGateway;
use Zend\Db\Sql\Select;
use Zend\Db\Sql\Sql;
use Zend\ServiceManager\ServiceLocatorAwareInterface;
use Zend\ServiceManager\ServiceLocatorInterface;
use Zend\Db\Adapter\Adapter;
use Zend\Db\Sql\Where;
use Search\Model\ImageRankingCandidate;


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
	public function getRankingCandidates($vw, $minNumVW=12) {
	
	   //SELECT COUNT(*), ImageId FROM `IFS` WHERE `VisualWord` 
	   //IN (3,5,4,6,108, 160, 577,1261,1427) GROUP BY ImageId ORDER BY 1 DESC
		
		
		if(!$vw)
		{
			//empty array rise error or return null
			return  array();
		}
		
		
		$adapter= $this->db;
		//$adapter->query('SELECT COUNT(*), ImageId FROM `IFS` WHERE `VisualWord` IN (?) GROUP BY ImageId ORDER BY 1 DESC ', $vw);
		
		
		$keyVW = array_keys($vw);
		$t = join(',', $keyVW);		

		$numVW = array($minNumVW);
		$statement = $adapter->createStatement('SELECT COUNT(*) as numVW, I.ImageId, IR.Representation, Concat(I.FileDirectory,I.FileName) as ImPath  FROM `IFS`, Images I, ImageRepresentations IR WHERE IFS.ImageId =I.ImageId and I.ImageId=IR.ImageId and `VisualWord` in ('.$t.') GROUP BY ImageId HAVING numVW>? ORDER BY 1 DESC LIMIT 80 ',$numVW);
		
		$result = $statement->execute();
		
		
		//TODO: remove hardcoded folder name
		$folerDir = '/pics/';
		$imgRank = array();
		foreach ($result as $row) {
			
			$rankCand = new ImageRankingCandidate();
			$rankCand->imageId =$row['ImageId'];
			$rankCand->vwIntersection =$row['numVW'];
			
			$rankCand->path = $folerDir. $row['ImPath'];
			
			
			$rankCand->representation = $this->getVisualWordsFromRep($row['Representation']);
			
			$rankCand->score = $rankCand->vwIntersection;
			
			$imgRank[]=$rankCand;
		}
		
		return  $imgRank;
		
	}
	
	
	/**
	 * Get visual wors array from string representation
	 * 
	 * assciative array { vwId1 => occurences, ...}
	 * 
	 * 
	 * @param string $imgRep
	 * @return array - 
	 */
	public function getVisualWordsFromRep($imgRep){
		
		//regexp - where representation in format {2035:1.0,2:1.0}
		$pattern = '/(\d+):(\d+.?\d*)/';
		//regexp - where representation in format {"2035":"1.0","2":"1.0"}
		$pattern = '/"(\d+)":"(\d+.?\d*)"/';
		//regexp - for both formats
		//$pattern = '/"?(\d+)"?:"?(\d+.?\d*)"?/';
		preg_match_all($pattern, $imgRep, $matches);
		
		
		$vw=$matches[1];
		$occur = $matches[2];
		
		if(! (empty($vw) || empty($occur)))
		{
			//combine new associative array form vw and occurences
			// vw are the keys and occur are the values
			$vw = array_combine($vw, $occur);
			//sort by keys
			ksort($vw);
		
		}
			
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