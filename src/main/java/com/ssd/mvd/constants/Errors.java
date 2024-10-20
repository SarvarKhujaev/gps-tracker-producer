package com.ssd.mvd.constants;

import com.ssd.mvd.inspectors.StringOperations;

import java.text.MessageFormat;

public enum Errors {
    DATA_NOT_FOUND {
        @Override
        @lombok.NonNull
        public String translate (
                @lombok.NonNull final String languageType
        ) {
            return switch ( languageType ) {
                case "ru" -> "НЕ НАЙДЕНО";
                case "uz" -> "TOPILMADI";
                default -> DATA_NOT_FOUND.name();
            };
        }
    },
    SERVICE_WORK_ERROR {
        @Override
        @lombok.NonNull
        public String translate (
                @lombok.NonNull final String languageType
        ) {
            return switch ( languageType ) {
                case "ru" -> "СЕРВИС НЕ РАБОТАЕТ";
                case "uz" -> "SERVICE ISHLAMAYAPTI";
                default -> SERVICE_WORK_ERROR.name();
            };
        }
    },
    PRIMARY_KEYS_NOT_FOUND {
        @Override
        @lombok.NonNull
        public String translate (
                @lombok.NonNull final String languageType,
                @lombok.NonNull final String entityName
        ) {
            return switch ( languageType ) {
                case "uz" -> "Primary keys topilmadi: %s".formatted( entityName );
                case "ru" -> "Primary keys для %s не найден".formatted( entityName );
                default -> "Primary keys for %s not found".formatted( entityName );
            };
        }
    },
    ENTITY_METHOD_IS_NOT_SYNCHRONIZED {
        @Override
        @lombok.NonNull
        public String translate (
                @lombok.NonNull final String entityName,
                @lombok.NonNull final String methodName
        ) {
            return MessageFormat.format(
                    """
                     Method: {0} of entity: {1} is not synchronized
                     """,
                    methodName,
                    entityName
            );
        }
    },
    ENTITY_METHOD_SHOULD_NOT_RETURN_NULL {
        @Override
        @lombok.NonNull
        public String translate (
                @lombok.NonNull final String entityName,
                @lombok.NonNull final String methodName
        ) {
            return MessageFormat.format(
                    """
                     Method: {0} of entity: {1} is not protected from null
                     """,
                    methodName,
                    entityName
            );
        }
    },

    WRONG_COLLECTION_TYPES_NUMBER,

    WRONG_TYPE_IN_ANNOTATION {
        @Override
        @lombok.NonNull
        @org.jetbrains.annotations.Contract( value = "_ -> _" )
        public String translate (
                @lombok.NonNull final String languageType
        ) {
            return String.join(
                    StringOperations.SPACE,
                    "Параметр:",
                    languageType,
                    "не имеет нужной аннотации"
            );
        }
    },
    FIELD_MIGHT_NOT_BE_EMPTY,
    ENTITY_IS_NOT_SUB_CLASS {
        @Override
        @lombok.NonNull
        @org.jetbrains.annotations.Contract( value = "_ -> _" )
        public String translate (
                @lombok.NonNull final String error
        ) {
            return String.join(
                    StringOperations.SPACE,
                    "Entity:",
                    error,
                    "is not subclass"
            );
        }
    },
    ENTITY_IS_NOT_TASK_ANNOTATED {
        @Override
        @lombok.NonNull
        @org.jetbrains.annotations.Contract( value = "_ -> _" )
        public String translate (
                @lombok.NonNull final String error
        ) {
            return String.join(
                    StringOperations.SPACE,
                    "Entity:",
                    error,
                    "is not annotated by TaskEntityAnnotations"
            );
        }
    },

    OBJECT_IS_IMMUTABLE {
        @Override
        @lombok.NonNull
        @org.jetbrains.annotations.Contract( value = "_ -> _" )
        public String translate (
                @lombok.NonNull final String error
        ) {
            return String.join(
                    StringOperations.SPACE,
                    "Entity:",
                    error,
                    "is immutable"
            );
        }
    },
    OBJECT_CONSTRUCTOR_IS_WRONG {
        @Override
        @lombok.NonNull
        @org.jetbrains.annotations.Contract( value = "_ -> _" )
        public String translate (
                @lombok.NonNull final String error
        ) {
            return String.join(
                    StringOperations.SPACE,
                    "Constructor of entity:",
                    error,
                    "is initialized in wrong way"
            );
        }
    },
    OBJECT_IS_OUT_OF_INSTANCE_PERMISSION {
        @Override
        @lombok.NonNull
        @org.jetbrains.annotations.Contract( value = "_, _ -> _" )
        public String translate (
                @lombok.NonNull final String languageType,
                @lombok.NonNull final String entityName
        ) {
            return String.join(
                    StringOperations.SPACE,
                    "Object:",
                    languageType,
                    "is out of permission list of:",
                    entityName
            );
        }
    },
    FIELD_AND_ANNOTATION_NAME_MISMATCH {
        @Override
        @lombok.NonNull
        @org.jetbrains.annotations.Contract( value = "_, _, _ -> _" )
        public String translate (
                @lombok.NonNull final String entityName,
                @lombok.NonNull final String fieldName,
                @lombok.NonNull final String annotationName
        ) {
            return String.join(
                    StringOperations.SPACE,
                    "Entity:",
                    entityName,
                    "with field:",
                    fieldName,
                    "is not the same as:",
                    annotationName
            );
        }
    };

    @lombok.NonNull
    @org.jetbrains.annotations.Contract( value = "_, _, _, _, _ -> _" )
    public String translate (
            @lombok.NonNull final String languageType,
            @lombok.NonNull final String fieldName,
            @lombok.NonNull final String entityName,
            final int paramsQuantityReceived,
            final int paramsQuantityExpected
    ) {
        return switch ( languageType ) {
            case "uz" -> "Collection uchun notog'ri parametrla berilgan %s %s %d %d".formatted(
                    fieldName,
                    entityName,
                    paramsQuantityReceived,
                    paramsQuantityExpected
            );
            case "ru" -> "Для коллекции %s параметра %s указано неверное количество типов, получено %d ожидаемо %d".formatted(
                    fieldName,
                    entityName,
                    paramsQuantityReceived,
                    paramsQuantityExpected
            );
            default -> "Wrong number of types for collection %s for field %s, received %d : expected %d".formatted(
                    fieldName,
                    entityName,
                    paramsQuantityReceived,
                    paramsQuantityExpected
            );
        };
    }

    @lombok.NonNull
    @org.jetbrains.annotations.Contract( value = "_, _, _ -> _" )
    public String translate (
            @lombok.NonNull final String languageType,
            @lombok.NonNull final String fieldName,
            @lombok.NonNull final String entityName
    ) {
        return switch ( languageType ) {
            case "uz" -> "Mumkin emas %s %s".formatted( fieldName, entityName );
            case "ru" -> "Формат null не допустим для %s в сущности: %s".formatted( fieldName, entityName );
            default -> "Type %s for %s CANNOT BE NULL".formatted( fieldName, entityName );
        };
    }

    @lombok.NonNull
    @org.jetbrains.annotations.Contract( value = "_, _ -> _" )
    public String translate (
            @lombok.NonNull final String languageType,
            @lombok.NonNull final String entityName
    ) {
        return switch ( languageType ) {
            case "uz" -> "Mumkin emas %s".formatted( entityName );
            case "ru" -> "Класс %s не имеет нужной аннотации ServiceParametrAnnotation".formatted( entityName );
            default -> "Type %s is unacceptable for ServiceParametrAnnotation annotation".formatted( entityName );
        };
    }

    @lombok.NonNull
    @org.jetbrains.annotations.Contract( value = "_ -> _" )
    public String translate (
            @lombok.NonNull final String languageType
    ) {
        return switch ( languageType ) {
            case "ru", "uz" -> StringOperations.EMPTY;
            default -> DATA_NOT_FOUND.name();
        };
    }
}
