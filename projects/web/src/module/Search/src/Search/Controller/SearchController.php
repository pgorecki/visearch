<?php
/**
 * Zend Framework (http://framework.zend.com/)
 *
 * @link      http://github.com/zendframework/Search for the canonical source repository
 * @copyright Copyright (c) 2005-2012 Zend Technologies USA Inc. (http://www.zend.com)
 * @license   http://framework.zend.com/license/new-bsd New BSD License
 */

namespace Search\Controller;

use Zend\Mvc\Controller\AbstractActionController; 
use Zend\View\Model\ViewModel;
use Search\Model\Image;          // <-- Add this import

use Search\Model\ImageRankingCandidate;
use Search\Model\Ranking;
use Search\Model\Ranking\EuclideanScore;



class SearchController extends AbstractActionController
{
	
	public $imageTable;
	
	
	public function getImageTable()
	{
		if (!$this->imageTable) {
			$sm = $this->getServiceLocator();
			$this->imageTable = $sm->get('Search\Model\ImageTable');
		}
		return $this->imageTable;
	}
	
	public function indexAction()
	{
		return new ViewModel(array(
				'images' => $this->getImageTable()->fetchAll(),
		));
	}

	public function searchrankingAction()
	{
		$data = $this->getServiceLocator()->get('Search\Model\SearchDBManager');
		
		$id = (int) $this->params()->fromRoute('id', 1);
		$img = $this->getImageTable()->getImage($id);
		
		if(!empty($img))
		{
			$imgRep = $data->getImgRepresentation($id);
			
			$picId = $imgRep['ImageId'];
			$picRep = $imgRep['Representation'];
			
			$vw = $data->getVisualWordsFromRep($picRep);
	
	
			$candidates =$data->getRankingCandidates($vw);
			if(!empty($candidates))
			{	
				$scoring = new EuclideanScore();			
				$candidates= $scoring->Score($vw,$candidates);
			}
			//$ranking = $data->getRankingForImage($imgRep);

		}
		
		return new ViewModel(array(
				'images' => $candidates,
				'image' => $img,
				'imgRep' =>$imgRep
		));
	}
	

    public function fooAction()
    {
        // This shows the :controller and :action parameters in default route
        // are working when you browse to /search/search/foo
        return array();
    }
}
