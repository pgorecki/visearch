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
		
		$id = 4;
		$img = $this->getImageTable()->getImage($id);
		
		
		$imgRep = $data->getImgRepresentation($id);
		
		
		//$ranking = $data->getRankingForImage($imgRep);
		
		$picId = $imgRep['ImageId'];
		$picRep = $imgRep['Representation'];
		
		return new ViewModel(array(
				'images' => array(),
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
