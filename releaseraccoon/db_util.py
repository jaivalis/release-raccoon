from pymysql import IntegrityError
from sqlalchemy.orm.exc import NoResultFound


def get_one_or_create(session,
                      model,
                      create_method='',
                      create_method_kwargs=None,
                      **kwargs):
    """ Convenience method

    A convenience method for looking up an object with the given kwargs (may be empty if your model has defaults for all
    fields), creating one if necessary.

    Returns a tuple of (object, created), where object is the retrieved or created object and created is a boolean
    specifying whether a new object was created. This is meant to prevent duplicate objects from being created when
    requests are made in parallel, and as a shortcut to boilerplatish code. For example:

    Important: This method is atomic assuming that the database enforces uniqueness of the keyword arguments.
    https://skien.cc/blog/2014/01/15/sqlalchemy-and-race-conditions-implementing-get_one_or_create/

    :param session:
    :param model:
    :param kwargs:
    :return:
    """
    try:
        return session.query(model).filter_by(**kwargs).one(), True
    except NoResultFound:
        kwargs.update(create_method_kwargs or {})
        created = getattr(model, create_method, model)(**kwargs)
        try:
            session.add(created)
            session.commit()
            return created, False
        except IntegrityError:
            session.rollback()
            return session.query(model).filter_by(**kwargs).one(), True
