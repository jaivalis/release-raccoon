

def get_or_create(session, model, **kwargs):
    """
    A convenience method for looking up an object with the given kwargs (may be empty if your model has defaults for all
    fields), creating one if necessary.

    Returns a tuple of (object, created), where object is the retrieved or created object and created is a boolean
    specifying whether a new object was created. This is meant to prevent duplicate objects from being created when
    requests are made in parallel, and as a shortcut to boilerplatish code. For example:

    Important: This method is atomic assuming that the database enforces uniqueness of the keyword arguments.

    :param session:
    :param model:
    :param kwargs:
    :return:
    """
    instance = session.query(model).filter_by(**kwargs).first()
    if instance:
        return instance
    else:
        instance = model(**kwargs)
        session.add(instance)
        session.commit()
        return instance
