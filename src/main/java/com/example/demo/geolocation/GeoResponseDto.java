package com.example.demo.geolocation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.List;


    @Getter
    public class GeoResponseDto {
        private Status status;
        private List<Result> results;

        @Getter
        public static class Status {
            private int code;
            private String name;
            private String message;
        }

        @Getter
        public static class Result {
            private String name;
            private Code code;
            private Region region;

            @Getter
            public static class Code {
                private String id;
                private String type;
                private String mappingId;
            }

            @Getter
            public static class Region {
                private Area area0;
                private Area area1;
                private Area area2;
                private Area area3;
                private Area area4;

                @Getter
                public static class Area {
                    private String name;
                }
            }
        }
    }

