package org.memetrader.ImageFlipMemeFetcherService;

import org.memetrader.common.MemeStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.logging.Logger;

/**
 * Fetches new memes from the imgflip meme api, adds it to the database as a new stock.
 */
@Service
@EnableScheduling
public class ImgFlipMemeFetcher {

    private final static Logger logger = Logger.getLogger(ImgFlipMemeFetcher.class.getName());

    private record MemeResponse(boolean success, MemeList data) {};
    private record MemeList(List<ImgFlipMeme> memes) {};
    private record ImgFlipMeme(int id, String name, String url) {};

    @Autowired
    private MemeStockRepository memeStockRepository;

    @Scheduled(fixedRate = 3600000)
    public void fetchNewMemes() {
        final String url = "https://api.imgflip.com/get_memes";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<MemeResponse> response = restTemplate.getForEntity(url, MemeResponse.class);
        List<ImgFlipMeme> fetchedMemes = response.getBody().data().memes();

        fetchedMemes.forEach((meme) -> {
            memeStockRepository.tryInsertMeme("imgflip" + meme.id(), meme.url(), meme.name());
        });

    }
}
