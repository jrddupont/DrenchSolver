package com.github.jrddupont.board

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import java.awt.Point

class BoardTest extends AnyFunSuite {

	val actualAdjacencyMatrix: Array[Array[Boolean]] = Array(
		Array( false,  true, false,  true,  true ),
		Array(  true, false,  true, false,  true ),
		Array( false,  true, false,  true,  true ),
		Array(  true, false,  true, false,  true ),
		Array(  true,  true,  true,  true, false )
	)

	val expectedAdjacencyMatrix: Array[Array[Boolean]] = Array(
		Array( false,  true,  true ),
		Array(  true, false, false ),
		Array(  true, false, false )
	)

	val r = 0
	val g = 1
	val b = 2

	// Red:0   Green:1   Blue:2
	val actualColorArray: Array[Int]   = Array( r, b, r, b, g )
	val expectedColorArray: Array[Int] = Array( r, b, b)

	val actualVolume: Array[Int]   = Array( 1, 5, 1, 5, 3 )
	val expectedVolume: Array[Int] = Array( 5, 5, 5)

	test("Test recolor logic") {
		val board = new Board(actualAdjacencyMatrix, actualColorArray, actualVolume)

		val regionToChange = 4

		val newBoard = board.colorRegion(regionToChange, r)

		newBoard.getRegionAdjacencyMatrix should be(expectedAdjacencyMatrix)
		newBoard.getRegionColorIDs should be(expectedColorArray)
		newBoard.getRegionVolumes should be(expectedVolume)

		assert( true )
	}
}