package entitymanager;

class EntityForPersistence {

    private Object entity;
    private Status status;

    public EntityForPersistence(Object entity, Status status) {
        this.entity = entity;
        this.status = status;
    }

    public static EntityForPersistence createOf(Object entity) {
        return new EntityForPersistence(entity, Status.CREATED);
    }

    public static EntityForPersistence updateOf(Object entity) {
        return new EntityForPersistence(entity, Status.UPDATED);
    }

    public static EntityForPersistence deleteOf(Object entity) {
        return new EntityForPersistence(entity, Status.DELETED);
    }

    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    public Status getStatus() {
        return status;
    }

    public Object getEntity() {
        return entity;
    }

    enum Status {
        CREATED, UPDATED, DELETED
    }
}
