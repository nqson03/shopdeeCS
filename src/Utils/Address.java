package Utils;

public record Address(String addressLine, Address.City city) {
    public enum City {
        HANOI("Hanoi"),
        HCMC("Ho Chi Minh City"),
        DANANG("Da Nang"),
        HAIPHONG("Hai Phong"),
        CANTHO("Can Tho");

        private final String string;
        City(String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    @Override
    public String toString() {
        return addressLine + ", " + city.toString();
    }
}
