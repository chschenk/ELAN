package mpi.dcr.isocat;

/**
 * A minimal Profile class, storing the name and the pid of the profile.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class Profile {
    private String id;
    private String name;

    /**
     * Creates a new Profile instance for the specified pid - name combination
     *
     * @param id the (short) pid
     * @param name the name of the profile
     */
    public Profile(String id, String name) {
        this.id = id;
        this.name = name;

        if (id == null) {
            this.id = "";
        }

        if (name == null) {
            this.name = "";
        }
    }

    /**
     * Creates a new Profile instance
     *
     * @param other the Profile to copy
     */
    public Profile(Profile other) {
        this.id = other.id;
        this.name = other.name;
    }

    /**
     * Returns the pid.
     *
     * @return the pid
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the pid.
     *
     * @param id the pid
     */
    public void setId(String id) {
        if (id != null) {
            this.id = id;
        }
    }

    /**
     * Returns the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the profile.
     *
     * @param name the name
     */
    public void setName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    /**
     * Returns the name of the profile
     *
     * @return the name of the profile
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns true if both "id" and "name" are equal.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object otherProf) {
        if (otherProf == null) {
            return false;
        }

        if (otherProf instanceof Profile) {
            // id and name are non-null
            if (((Profile) otherProf).getId().equals(id) &&
                    ((Profile) otherProf).getName().equals(name)) {
                return true;
            }
        }

        return false;
    }
}
