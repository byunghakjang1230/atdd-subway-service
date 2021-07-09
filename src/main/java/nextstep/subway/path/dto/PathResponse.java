package nextstep.subway.path.dto;

import java.util.List;
import java.util.stream.Collectors;

import nextstep.subway.path.domain.Path;
import nextstep.subway.path.util.FareCalculator;

public class PathResponse {

    private List<VertexResponse> stations;
    private int distance;
    private int totalFare;

    protected PathResponse() {
    }

    private PathResponse(List<VertexResponse> stations, int distance) {
        this.stations = stations;
        this.distance = distance;
    }

    private PathResponse(List<VertexResponse> stations, int distance, int totalFare) {
        this.stations = stations;
        this.distance = distance;
        this.totalFare = totalFare;
    }

    public static PathResponse of(Path path) {
        List<VertexResponse> vertexResponses = path.getPathVertexes()
                .stream()
                .map(VertexResponse::of)
                .collect(Collectors.toList());
        return new PathResponse(vertexResponses, path.getTotalDistance());
    }

    public static PathResponse of(Path path, FareCalculator fareCalculator) {
        List<VertexResponse> vertexResponses = path.getPathVertexes()
                .stream()
                .map(VertexResponse::of)
                .collect(Collectors.toList());
        return new PathResponse(vertexResponses, path.getTotalDistance(), fareCalculator.getFare());
    }

    public List<VertexResponse> getStations() {
        return this.stations;
    }

    public int getDistance() {
        return this.distance;
    }

    public int getTotalFare() {
        return totalFare;
    }
}
