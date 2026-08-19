package br.com.mentorhub.identity.application;

import br.com.mentorhub.identity.api.dto.LinkedInPreviewResponse;
import br.com.mentorhub.identity.domain.LinkedInProfileParser;
import org.springframework.stereotype.Service;

@Service
public class PreviewLinkedInProfileService {

    public LinkedInPreviewResponse execute(String url) {
        LinkedInProfileParser.Preview preview = LinkedInProfileParser.parse(url);
        return new LinkedInPreviewResponse(preview.url(), preview.username(), preview.suggestedName());
    }
}
