package jackpot.orm.repository;

import java.util.List;

public interface JackpotRepository<ID_CLASS, TABLE_CLASS> {

    TABLE_CLASS findOne(ID_CLASS id, TABLE_CLASS anyObj);

    List<TABLE_CLASS> findAll();

    void delete(ID_CLASS id, TABLE_CLASS anyObj);

    TABLE_CLASS save(TABLE_CLASS obj);

}
