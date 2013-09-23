<?php

namespace Search\Model\Ranking;

abstract class RankingScore {
	
	
	public function sortCandidates($candidates){
		
		usort($candidates,array($this,'sortCompare'));
		
		return $candidates;
		
	}
	
	public static  function sortCompare($a,$b)
	{
		return $a->score == $b->score ? 0 : ($a->score > $b->score) ? 1:-1;
		
	}
	
	public abstract  function ComputeScore($imRep1, $imRep2);
	
	public function Score($imRep, $candidates){
	
	
	
		foreach ($candidates as $im) {
			$im->score = $this->ComputeScore($imRep, $im->representation);
		}
		
		
		return $this->sortCandidates($candidates);
	
		//return $candidates;
	}
}

?>