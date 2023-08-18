package models;

/**
 * Model/data class models.Programme represents a progamme object.
 *
 * @author Alireza Ramezani, id19ari
 * @version 2.0
 */
public class Programme {
    public final String name;
    public final String LTStart;
    public final String LTEnd;
    public final String imagePath;
    public final String description;

    private Programme(ProgrammeBuilder programmeBuilder) {
        this.name = programmeBuilder.name;
        this.LTStart = programmeBuilder.UTCstart;
        this.LTEnd = programmeBuilder.UTCend;
        this.imagePath = programmeBuilder.imagePath;
        this.description = programmeBuilder.description;
    }

    /**
     * Builder that builds the programme and handles potential null arguments
     */
    public static class ProgrammeBuilder {
        private final String name;
        private final String UTCstart;
        private final String UTCend;
        private String imagePath;
        private String description;

        public ProgrammeBuilder(String name, String UTCstart, String UTCend) {
            this.name = name;
            this.UTCstart = UTCstart;
            this.UTCend = UTCend;
        }

        /**
         * Set the image path
         * @param imagePath String with image path
         * @return ProgrammeBuilder
         */
        public ProgrammeBuilder setImagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        /**
         * Set the image path
         * @param description String with the description
         * @return ProgrammeBuilder
         */
        public ProgrammeBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Build the programme, if imagepath or description is null, assign placeholders instead.
         * @return Programme
         */
        public Programme buildProgramme() {
            if (this.imagePath == null)
                this.imagePath = "https://static-cdn.sr.se/images/" +
                        "5380/a7898d6c-786f-4fcb-b68e-c5f56f4b3bef.jpg?preset=api-default-square";
            if (this.description == null)
                this.description = "Ingen beskrivning tillgänglig";
            return new Programme(this);
        }
    }
}
