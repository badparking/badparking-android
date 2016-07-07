package ua.in.badparking.api.requests;

import java.util.List;

import ua.in.badparking.model.Claim;
import ua.in.badparking.model.User;

public class ClaimRequest {

    private Claim claimSerializer;

    public ClaimRequest(Claim claim, User user, List<Claim.MediaFileSerializer> images) {
        claimSerializer = claim;
        claimSerializer.setUser(user);
        claimSerializer.setPhotoFiles(images);
    }
}
