/**
 * This class represents the basic flight controller of the Bereshit space craft.
 *
 * @author ben-moshe
 */
public class Bereshit_101 {
    public static final double WEIGHT_EMP = 165; // kg
    public static final double WEIGHT_FULE = 420; // kg
    public static final double WEIGHT_FULL = WEIGHT_EMP + WEIGHT_FULE; // kg
    // https://davidson.weizmann.ac.il/online/askexpert/%D7%90%D7%99%D7%9A-%D7%9E%D7%98%D7%99%D7%A1%D7%99%D7%9D-%D7%97%D7%9C%D7%9C%D7%99%D7%AA-%D7%9C%D7%99%D7%A8%D7%97
    public static final double MAIN_ENG_F = 430; // N
    public static final double SECOND_ENG_F = 25; // N
    public static final double MAIN_BURN = 0.15; //liter per sec, 12 liter per m'
    public static final double SECOND_BURN = 0.009; //liter per sec 0.6 liter per m'
    public static final double ALL_BURN = MAIN_BURN + 8 * SECOND_BURN;

    public static double accMax(double weight) {
        return acc(weight, true, 8);
    }

    public static double acc(double weight, boolean main, int seconds) {
        double t = 0;
        if (main) {
            t += MAIN_ENG_F;
        }
        t += seconds * SECOND_ENG_F;
        double ans = t / weight;
        return ans;
    }

    // 14095, 955.5, 24.8, 2.0
    public static void main(String[] args) {

        System.out.println("Simulating Bereshit's Landing:");
        // starting point:
        double verticalSpeed = 24.8;
        double horizontalSpeed = 932.2;
        double dist = 181 * 1000;
        double angle = 58.3; // zero is vertical (as in landing)
        double altitude = 13748; // 2:25:40 (as in the simulation) // https://www.youtube.com/watch?v=JJ0VfRL9AMs
        double time = 0;
        double dt = 1; // sec
        double acceleration = 0; // Acceleration rate (m/s^2)
        double fuel = 121; //
        double weight = WEIGHT_EMP + fuel;
        System.out.println("time, verticalSpeed, horizontalSpeed, dist, altitude, angle,weight,acceleration");
        double NN = 0.7; // rate[0,1]

        // ***** main simulation loop ******
        while (altitude > 1) {
            if (time % 10 == 0 || altitude < 100) {
                System.out.println("fuel= " + fuel + "time= " + time + ", verticalSpeed= " + verticalSpeed + ", horizontalSpeed= " + horizontalSpeed + ", dist= " + dist + ", altitude= " + altitude + ", angle= " + angle + ", weight= " + weight + ", acceleration= " + acceleration);
            }
            // over 2 km above the ground
            if (altitude > 2000) {    // maintain a vertical speed of [20-25] m/s
                if (angle > 59.3) {
                    angle -= 0.8;
                } else if (angle < 59.3) {
                    angle += 0.8;
                }
                if (verticalSpeed > 26) {
                    NN += 0.003 * dt;
                } // more power for braking
                if (verticalSpeed < 19) {
                    NN -= 0.003 * dt;
                } // less power for braking
            }
            // lower than 2 km - horizontal speed should be close to zero
            else {
                angle -= 0.75;
                if (angle > 3)
                    angle -= 0.75;
                if (altitude > 1500) {
                    NN = 0.5;
                } else if (altitude > 1000) {
                    NN = 0.58;
                } else if (altitude > 500) {
                    NN = 0.6;
                } else if (altitude > 100) {
                    NN = 0.70;
                    if (verticalSpeed < 20) NN = 0.65;
                } else if (altitude > 40) {
                    NN = 0.75;
                    if (verticalSpeed < 10 && verticalSpeed > 5) {
                        NN = 0.7;
                    }
                }

            }


//        }
            if (altitude < 5) { // no need to stop
                NN = 0.3;
            }
            // main computations
            double ang_rad = Math.toRadians(angle);
            double h_acc = Math.sin(ang_rad) * acceleration;
            double v_acc = Math.cos(ang_rad) * acceleration;
            double vacc = Moon.getAcc(horizontalSpeed);
            time += dt;
            double dw = dt * ALL_BURN * NN;
            if (fuel > 0) {
                fuel -= dw;
                weight = WEIGHT_EMP + fuel;
                acceleration = NN * accMax(weight);
            } else { // ran out of fuel
                acceleration = 0;
            }

            v_acc -= vacc;
            if (horizontalSpeed > 0) {
                horizontalSpeed -= h_acc * dt;
            }
            dist -= horizontalSpeed * dt;
            verticalSpeed -= v_acc * dt;
            altitude -= dt * verticalSpeed;


        }
        System.out.println("landing ");

    }
}
