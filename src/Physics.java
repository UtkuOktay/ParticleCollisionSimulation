public class Physics {

    //Keeps the colliding balls in the previous frame.
    private static boolean[][] collisionMatrix;

    //Checks collisions and handle if any. Also, updates the positions of the particles.
    public static void update(Particle[] particles, double dt) {

        handleParticleCollisions(particles); //Call handleParticleCollisions method to detect and handle them.

        for (int i = 0; i < particles.length; i++) { //Iterating over each particle.
            //The reason it does not multiply the related velocity value by -1 is that since it is a
            // discrete collision detection algorithm, a particle may be colliding with a wall in consecutive frames.
            //As a result, the particle can get stuck at there.

            if (collidesWithLeftWall(particles[i])) //If the particle collides with the left wall
                particles[i].setxVelocity(Math.abs(particles[i].getxVelocity())); //Make the x velocity positive.

            else if (collidesWithRightWall(particles[i])) //If the particle collides with the right wall
                particles[i].setxVelocity(Math.abs(particles[i].getxVelocity()) * -1.0); //Make the x velocity negative.

            if (collidesWithCeiling(particles[i])) //If the particle collides with the ceiling
                particles[i].setyVelocity(Math.abs(particles[i].getyVelocity())); //Make the y velocity positive.

            else if (collidesWithFloor(particles[i])) //If the particle collides with the floor
                particles[i].setyVelocity(Math.abs(particles[i].getyVelocity()) * -1.0); //Make the y velocity negative.

            particles[i].setxLocation((particles[i].getxLocation() + particles[i].getxVelocity() * dt / 1000)); //Update the position in the x-axis.
            particles[i].setyLocation((particles[i].getyLocation() + particles[i].getyVelocity() * dt / 1000)); //Update the position in the y-axis.
        }
    }

    //Returns true if the particle collides with the left wall.
    private static boolean collidesWithLeftWall(Particle particle) {
        //If the distance between the center of the particle and the wall is smaller than its radius, it means they collide.
        return particle.getxLocation() - particle.getRadius() < 0;
    }

    //Returns true if the particle collides with the right wall.
    private static boolean collidesWithRightWall(Particle particle) {
        //If the distance between the center of the particle and the wall is smaller than its radius, it means they collide.
        return particle.getxLocation() + particle.getRadius() > Main.width;
    }

    //Returns true if the particle collides with the ceiling.
    private static boolean collidesWithCeiling(Particle particle) {
        //If the distance between the center of the particle and the wall is smaller than its radius, it means they collide.
        return particle.getyLocation() - particle.getRadius() < 0;
    }

    //Returns true if the particle collides with the floor.
    private static boolean collidesWithFloor(Particle particle) {
        //If the distance between the center of the particle and the wall is smaller than its radius, it means they collide.
        return particle.getyLocation() + particle.getRadius() > Main.height;
    }

    //Calculates the final velocities of colliding particles using the conversation of momentum and kinetic energy.
    //We can use conversation of kinetic energy as the collisions are perfectly elastic.
    private static void handleParticleCollisions(Particle[] particles) {
        //Checks every possible pair of particles. It is simple, but has a time complexity of O(n^2). Can run slowly with big amount of particles.
        for (int i = 0; i < particles.length; i++) {
            for (int j = 0; j < particles.length; j++) {
                if (i == j) //To avoid checking a particle with itself.
                    break;

                //Checks if the particles collide.
                if (!particlesCollide(particles[i], particles[j])) {
                    collisionMatrix[i][j] = false; //If the particles do not collide, set their collision value as false.
                    continue; //Terminate the iteration.
                }

                //If the particles collided in the previous frame terminate the iteration.
                //The reason for this operation is that sometimes even though the velocities of the particles are updated after their collision,
                // they might be colliding in the next frames. As a result, their velocities updated again and they may stick to each other.
                //This technique may cause some bugs which cause the particles to behave in an unexpected way, but in my opinion, it is better than sticking particles.
                if (collisionMatrix[i][j])
                    continue;

                //If the program comes here, it means the collision should be handled.
                collisionMatrix[i][j] = true;

                Particle p1 = particles[i];
                Particle p2 = particles[j];

                //Although it is a 2D simulation, it is thought as a 2D projection of a 3D environment in which the movements of the particles are limited to one plane.
                //That is, since there is no force in the 3rd dimension, they do not move in that direction.
                //The density of each particle is accepted the same. However, their size changes and the volume of a sphere is proportional to the cube of its radius.
                //Hence, its mass is also proportional to the cube of its radius.
                double m1 = Math.pow(p1.getRadius(), 3);
                double m2 = Math.pow(p2.getRadius(), 3);

                //Normal vector, with the size of the distance between the centers of the particles.
                double[] n = {p2.getxLocation() - p1.getxLocation(), p2.getyLocation() - p1.getyLocation()};
                double lengthOfn = Math.sqrt(Math.pow(n[0], 2) + Math.pow(n[1], 2)); //Length of vector n.

                double[] un = {n[0] / lengthOfn, n[1] / lengthOfn}; //Normalizing the vector n. That is, its length will be 1.
                double[] ut = {-un[1], un[0]}; //Tangent vector.

                double[] v1 = {p1.getxVelocity(), p1.getyVelocity()}; //Velocity of the first particle, represented as a 2D vector.
                double[] v2 = {p2.getxVelocity(), p2.getyVelocity()}; //Velocity of the second particle, represented as a 2D vector.

                //Velocities of particles in normal and tangential directions.
                double v1n = dotProduct(un, v1);
                double v1t = dotProduct(ut, v1);

                double v2n = dotProduct(un, v2);
                double v2t = dotProduct(ut, v2);

                //Normal velocities after the collision.
                double v1nAfterCollision = (v1n * (m1 - m2) + 2 * m2 * v2n) / (m1 + m2);
                double v2nAfterCollision = (v2n * (m2 - m1) + 2 * m1 * v1n) / (m1 + m2);

                //Finding velocity vectors after collision.
                double[] v1nAfterCollisionVector = multiplyVectorByAScalar(un, v1nAfterCollision);
                double[] v1tAfterCollisionVector = multiplyVectorByAScalar(ut, v1t);

                double[] v2nAfterCollisionVector = multiplyVectorByAScalar(un, v2nAfterCollision);
                double[] v2tAfterCollisionVector = multiplyVectorByAScalar(ut, v2t);

                //Final velocities.
                double[] v1AfterCollision = sumOfTwoVectors(v1nAfterCollisionVector, v1tAfterCollisionVector);
                double[] v2AfterCollision = sumOfTwoVectors(v2nAfterCollisionVector, v2tAfterCollisionVector);

                //Assigning the values.
                p1.setxVelocity(v1AfterCollision[0]);
                p1.setyVelocity(v1AfterCollision[1]);

                p2.setxVelocity(v2AfterCollision[0]);
                p2.setyVelocity(v2AfterCollision[1]);
            }
        }
    }

    //Checks if the particles collide with each other. Returns true if the distance between their centers is
    // equal or smaller than the sum of their radiuses.
    private static boolean particlesCollide(Particle particle1, Particle particle2) {
        double distanceInX = particle1.getxLocation() - particle2.getxLocation();
        double distanceInY = particle1.getyLocation() - particle2.getyLocation();
        double distanceBetweenCenters = Math.sqrt(Math.pow(distanceInX, 2) + Math.pow(distanceInY, 2));
        return distanceBetweenCenters <= particle1.getRadius() + particle2.getRadius();
    }

    //Calculates and returns the dot product of the given vectors.
    private static double dotProduct(double[] u1, double[] u2) {
        if (u1.length != u2.length)
            throw new IllegalArgumentException("The length of the vectors must be the same.");

        double result = 0;

        for (int i = 0; i < u1.length; i++) {
            result += u1[i] * u2[i];
        }

        return result;
    }

    //Creates and returns a new vector which is the multiplication of the given vector and the scalar.
    private static double[] multiplyVectorByAScalar(double[] u, double number) {
        double[] result = new double[u.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = u[i] * number;
        }

        return result;
    }

    //Creates and returns a new vector which is the sum of the given vectors.
    private static double[] sumOfTwoVectors(double[] u1, double[] u2) {
        if (u1.length != u2.length)
            throw new IllegalArgumentException("The length of the vectors must be the same.");

        double[] result = new double[u1.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = u1[i] + u2[i];
        }

        return result;
    }

    //Initializes the collision matrix with the given parameter.
    public static void initializeCollisionMatrix(int numberOfParticles) {
        if (numberOfParticles < 0)
            throw new IllegalArgumentException("Number of particles cannot be smaller than 0.");

        collisionMatrix = new boolean[numberOfParticles][numberOfParticles];
    }
}
