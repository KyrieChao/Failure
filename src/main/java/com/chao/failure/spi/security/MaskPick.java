package com.chao.failure.spi.security;

/**
 * Resolves a {@link Mask} from a field path.
 *
 * <p>This SPI allows applications to plug in custom rules to map field paths
 * (e.g. {@code "headers.accessToken"}, {@code "user.password"}) to a logical {@link Mask} type.</p>
 *
 * <p>Return {@code null} if the path is not recognized.</p>
 *
 * @author Kyrie Chao
 * @version 1.3.1
 */
public interface MaskPick {
    /**
     * Resolve a {@link Mask} from the given field path.
     *
     * @param pick field path, may be {@code null}
     * @return resolved mask type, or {@code null} if not recognized
     */
    Mask resolve(String pick);
}
