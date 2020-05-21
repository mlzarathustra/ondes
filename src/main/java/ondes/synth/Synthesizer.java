package ondes.synth;

public class Synthesizer extends Thread {
    Snake snake = new Snake();
    Instant instant;

    Synthesizer(int sampleRate) {
        instant = new Instant(sampleRate);
    }



    public void run() {

        for (;;) {
            instant.next();
            snake.update();



        }

    }




}
