<?php

namespace Search\Model\Ranking;

use Search\Model\Ranking\RankingScore;



class EuclideanScore extends RankingScore {
	
	public  $p=2.0;
	
	public function ComputeScore($imRep1, $imRep2)
	{
		
		$imRep1[10000000]=0.0;
		$imRep2[10000000]=0.0;
		
		
		reset($imRep1);
		reset($imRep2);
		
		$sum = 0.0;
		
		while(current($imRep1) && current($imRep2) )
		{
			$key1 = key($imRep1);
			$key2 = key($imRep2);
			
			if($key1==$key2){
				$sum = $sum+  pow($imRep1[$key1]-$imRep2[$key2],$this->p);
				next($imRep1);
				next($imRep2);
			} else if($key1<$key2)
			{
				$sum = $sum + pow($imRep1[$key1],$this->p);
				next($imRep1);
				
			}  else if($key2<$key1)
			{
				$sum = $sum + pow($imRep2[$key2],$this->p);
				next($imRep2);
			}
			
		}
		
		$sum = pow($sum,1/$this->p);
		
		return $sum;
		
		
		
	}
}

?>