package io.springbox.recommendations.services;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author Vinicius Carvalho
 */
@Service
public class RecommendationService {



	@Autowired
	private DataModel dataModel;

	private Recommender userRecommender;
	private GenericItemBasedRecommender movieRecommender;

	@PostConstruct
	public void init() throws Exception{
		this.userRecommender = userRecommender();
		this.movieRecommender = movieRecommender();
	}



	public Recommender userRecommender() throws Exception{
		PearsonCorrelationSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
		UserNeighborhood neighborhood = new NearestNUserNeighborhood(5,similarity,dataModel);
		return new CachingRecommender(new GenericUserBasedRecommender(dataModel,neighborhood,similarity));
	}



	public GenericItemBasedRecommender movieRecommender() throws Exception{
		PearsonCorrelationSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
		CachingItemSimilarity itemSimilarity = new CachingItemSimilarity(similarity,dataModel);
		return new GenericItemBasedRecommender(dataModel,itemSimilarity);
	}


	public List<RecommendedItem> userRecommendations(Long userId){
		try {
			return userRecommender.recommend(userId,10);
		}
		catch (TasteException e) {
			throw new RuntimeException("Could not compute recommendation for user");
		}
	}

	public List<RecommendedItem> movieRecommendations(Long movieId){
		try {
			return movieRecommender.mostSimilarItems(movieId,10);
		}
		catch (TasteException e) {
			throw new RuntimeException("Could not compute recommendation for movie");
		}
	}

	public Recommender getUserRecommender() {
		return userRecommender;
	}

	public Recommender getMovieRecommender() {
		return movieRecommender;
	}
}
