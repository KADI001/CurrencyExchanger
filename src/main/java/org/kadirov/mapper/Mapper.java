package org.kadirov.mapper;

import org.kadirov.mapper.exception.MappingException;

public interface Mapper<TSource, TResult> {
    TResult map(TSource source) throws MappingException;
}
