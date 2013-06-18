/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.renjin.primitives;

import org.apache.commons.math.special.Beta;
import org.apache.commons.math.special.Gamma;
import org.apache.commons.math.util.MathUtils;
import org.renjin.invoke.annotations.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;


/**
 * Math functions not found in java.Math or apache commons math
 */
public class MathExt {

  private MathExt() {
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double gamma(double x) {
    return Math.exp(Gamma.logGamma(x));
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double sign(double x) {
    return Math.signum(x);
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double log(double x, double base) {

    //Method cannot be called directly as R and Apache Commons Math argument order
    // are reversed
    return MathUtils.log(base, x);
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double log(double d) {
    return Math.log(d);
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double log2(double d) {
    return MathUtils.log(2, d);
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double abs(double x) {
    return Math.abs(x);
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double asinh(double val) {
    return (Math.log(val + Math.sqrt(val * val + 1)));
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double acosh(double val) {
    return (Math.log(val + Math.sqrt(val + 1) * Math.sqrt(val - 1)));
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double atanh(double val) {
    return (0.5 * Math.log((1 + val) / (1 - val)));
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double atan2(double y, double x) {
    return (Math.atan2(y, x));
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double signif(double x, int digits) {
    return new BigDecimal(x).round(new MathContext(digits, RoundingMode.HALF_UP)).doubleValue();
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double expm1(double x) {
    return Math.expm1(x);
  }

  @Deferrable
  @Primitive
  @DataParallel
  public static double log1p(double x) {
    return Math.log1p(x);
  }

  @Primitive
  @Deferrable
  @DataParallel
  public static double beta(double a, double b) {
    return (Math.exp(Beta.logBeta(a, b)));
  }

  @Primitive
  @Deferrable
  @DataParallel
  public static double lbeta(double a, double b) {
    return (Beta.logBeta(a, b));
  }

  @Primitive
  @DataParallel
  public static double choose(double n, int k) {
    /*
     * Because gamma(a+1) = factorial(a)
     * we use gamma(n+1) /(gamma(n-k+1) * gamma(k+1)) instead of
     * Binomial(n,k) = n! / ((n-k)! * k!) for non-integer n values.
     * 
     */
    if (k < 0) {
      return (0);
    } else if (k == 0) {
      return (1);
    } else if ((int) n == n) {
      return (MathUtils.binomialCoefficientDouble((int) n, k));
    } else {
      return (MathExt.gamma(n + 1) / (MathExt.gamma(n - k + 1) * MathExt.gamma(k + 1)));
    }
  }

  @Primitive
  @DataParallel
  public static double lchoose(double n, int k) {
    return (Math.log(choose(n, k)));
  }
  
  
  // our wrapper generator gets confused by the two double & float overloads
  // of Math.round
  @Primitive
  @Deferrable
  @DataParallel
  public static double round(double x) {
    return Math.round(x);
  }
  
  @Primitive
  @Deferrable
  @DataParallel
  public static double round(double x, int digits) {
    double factor = Math.pow(10, digits);
    return Math.round(x * factor) / factor;
  }
  
  @Generic
  @Deferrable
  @Primitive("trunc")
  @DataParallel
  public static double truncate(double x){
    return Math.floor(x);
  }
}
