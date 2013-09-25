<?php 
namespace Search\Model;


use Zend\InputFilter\InputFilter;
use Zend\InputFilter\InputFilterAwareInterface;
use Zend\InputFilter\InputFilterInterface;

 class Image implements InputFilterAwareInterface
 {
     public $imageId;
     public $fileName;
     public $fileDirectory;
     public $url;
     public $created;
     
     
     public $imagePath;
     
     protected $inputFilter;

     public function exchangeArray($data)
     {
         $this->imageId     = (!empty($data['ImageId'])) ? $data['ImageId'] : null;
         $this->fileName = (!empty($data['FileName'])) ? $data['FileName'] : null;
         $this->fileDirectory  = (!empty($data['FileDirectory'])) ? $data['FileDirectory'] : null;
         $this->url  = (!empty($data['URL'])) ? $data['URL'] : null;
         $this->created  = (!empty($data['Created'])) ? $data['Created'] : null;
     }
     
     public function setInputFilter(InputFilterInterface $inputFilter)
     {
     	throw new \Exception("Not used");
     }
     public function getInputFilter()
     {
     	throw new \Exception("Not used");
     	
     }
     
/** 
     public function setInputFilter(InputFilterInterface $inputFilter)
     {
     	throw new \Exception("Not used");
     }
     
     public function getInputFilter()
     {
     	if (!$this->inputFilter) {
     		$inputFilter = new InputFilter();
     
     		$inputFilter->add(array(
     				'name'     => 'id',
     				'required' => true,
     				'filters'  => array(
     						array('name' => 'Int'),
     				),
     		));
     
     		$inputFilter->add(array(
     				'name'     => 'artist',
     				'required' => true,
     				'filters'  => array(
     						array('name' => 'StripTags'),
     						array('name' => 'StringTrim'),
     				),
     				'validators' => array(
     						array(
     								'name'    => 'StringLength',
     								'options' => array(
     										'encoding' => 'UTF-8',
     										'min'      => 1,
     										'max'      => 100,
     								),
     						),
     				),
     		));
     
     		$inputFilter->add(array(
     				'name'     => 'title',
     				'required' => true,
     				'filters'  => array(
     						array('name' => 'StripTags'),
     						array('name' => 'StringTrim'),
     				),
     				'validators' => array(
     						array(
     								'name'    => 'StringLength',
     								'options' => array(
     										'encoding' => 'UTF-8',
     										'min'      => 1,
     										'max'      => 100,
     								),
     						),
     				),
     		));
     
     		$this->inputFilter = $inputFilter;
     	}
     
     	return $this->inputFilter;
     }
      **/
 }
