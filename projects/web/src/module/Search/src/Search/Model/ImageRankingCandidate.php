<?php

namespace Search\Model;

class ImageRankingCandidate {

	public $imageId;
	public $vwIntersection;
	
	/**
	 * Path to image
	 * @var string
	 */
	public $path;
	
	
	/**
	 * Asoc array with image representation
	 * @var array
	 */
	public $representation;
	
	/**
	 * Ranking score, smaller the better
	 * @var float
	 */
	public $score;
	
}

?>